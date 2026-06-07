package com.pathkeeper.alert.consumer;

import com.pathkeeper.alert.domain.fcm.FcmClient;
import com.pathkeeper.alert.domain.fcm.FcmException;
import com.pathkeeper.alert.domain.fcm.FcmSendResult;
import com.pathkeeper.alert.domain.guardian.GuardianInfo;
import com.pathkeeper.alert.domain.guardian.GuardianResolver;
import com.pathkeeper.alert.domain.idempotency.IdempotencyGuard;
import com.pathkeeper.alert.domain.notification.DepartureEventRepository;
import com.pathkeeper.alert.domain.notification.NotificationBuilder;
import com.pathkeeper.common.constant.KafkaTopics;
import com.pathkeeper.common.dto.AlertEvent;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 이탈 알림 이벤트를 받아 FCM 알림을 발송하는 메인 컨슈머.
 *
 * 처리 순서:
 * 1. 멱등성 검사 (중복 처리 방지)
 * 2. DepartureEvent 기록 (DB)
 * 3. 보호자 조회
 * 4. 각 보호자에게 FCM 전송
 * 5. 발송 완료 표시
 * 6. ack
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertConsumer {

    private final IdempotencyGuard idempotencyGuard;
    private final GuardianResolver guardianResolver;
    private final FcmClient fcmClient;
    private final NotificationBuilder notificationBuilder;
    private final DepartureEventRepository departureEventRepository;
    private final MeterRegistry meterRegistry;

    @KafkaListener(
            topics = KafkaTopics.ALERT_EVENTS,
            groupId = "alert-sender-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Timed(value = "alert.process", description = "알림 처리 전체 시간")
    public void consume(
            @Payload AlertEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment ack) {

        log.info("알림 수신: userId={}, type={}, partition={}, offset={}",
                event.userId(), event.type(), partition, offset);

        // === 1단계: 멱등성 검사 ===
        if (!idempotencyGuard.tryAcquire(topic, partition, offset)) {
            // 이미 처리됨 → 그냥 ack
            ack.acknowledge();
            meterRegistry.counter("alert.skipped.duplicate").increment();
            return;
        }

        try {
            processAlert(event);
            ack.acknowledge();

        } catch (FcmException e) {
            // FCM 재시도 가능 실패 → 멱등성 키는 유지 (DLQ로 이동)
            // 키를 해제하면 재시도 시 departure_events 중복 삽입이 발생하므로 유지
            log.error("FCM 전송 실패 (DLQ 이동): userId={}", event.userId(), e);
            ack.acknowledge();
            meterRegistry.counter("fcm.failed.retryable").increment();

        } catch (Exception e) {
            // 영구 실패 → 멱등성 키 유지
            log.error("처리 실패 (영구): userId={}", event.userId(), e);
            ack.acknowledge();
            meterRegistry.counter("alert.failed.permanent").increment();
        }
    }

    private void processAlert(AlertEvent event) {
        // === 2단계: 보호자 조회 (FCM 전송 전 확인, DB 기록보다 먼저 수행) ===
        List<GuardianInfo> guardians = guardianResolver.findGuardiansOf(event.userId());

        if (guardians.isEmpty()) {
            log.warn("보호자 없음: protegeId={}", event.userId());
            meterRegistry.counter("alert.no.guardian").increment();
            return;
        }

        // === 3단계: DepartureEvent 기록 ===
        // FCM 재시도 시 중복 삽입 방지: 보호자 조회 후에 기록하고,
        // DB 삽입은 멱등성 키가 보장하는 범위 안에서 한 번만 실행됨
        Long departureEventId = null;
        if (event.type() == AlertEvent.AlertType.DEPARTURE) {
            departureEventId = departureEventRepository.insertDeparture(
                    event.userId(),
                    event.safeZoneId(),
                    event.lat(),
                    event.lng(),
                    event.occurredAt()
            );
            log.debug("DepartureEvent 기록: id={}", departureEventId);
        }

        // === 4단계: 각 보호자에게 FCM 전송 ===
        String protegeName = guardianResolver.findProtegeName(event.userId());
        NotificationBuilder.Notification notification =
                notificationBuilder.build(event, protegeName);

        boolean anySent = false;
        for (GuardianInfo guardian : guardians) {
            boolean sent = sendToGuardian(guardian, notification);
            if (sent) {
                anySent = true;
            }
        }

        // === 5단계: 발송 완료 표시 ===
        if (anySent && departureEventId != null) {
            departureEventRepository.markNotified(departureEventId);
        }

        meterRegistry.counter("alert.processed").increment();
    }

    /**
     * 한 보호자에게 FCM 전송. 결과에 따른 처리 분기.
     *
     * @return 전송 성공 여부
     */
    private boolean sendToGuardian(GuardianInfo guardian,
                                   NotificationBuilder.Notification notification) {
        FcmSendResult result = fcmClient.send(
                guardian.getFcmToken(),
                notification.title(),
                notification.body()
        );

        if (result.success()) {
            log.info("FCM 전송 성공: guardianId={}, messageId={}",
                    guardian.getGuardianId(), result.messageId());
            meterRegistry.counter("fcm.sent.success").increment();
            return true;
        }

        // 실패 처리
        switch (result.failureType()) {
            case RETRYABLE -> {
                // 재시도 가능 → 예외 throw → ErrorHandler가 재시도
                meterRegistry.counter("fcm.failed.retryable").increment();
                throw new FcmException("FCM 일시 실패: " + result.errorMessage());
            }
            case INVALID_TOKEN -> {
                // 토큰 무효화 → DB, 캐시에서 토큰 제거 → 다음번엔 이 보호자 제외
                log.warn("토큰 무효: guardianId={}, 토큰 삭제", guardian.getGuardianId());
                guardianResolver.invalidateToken(guardian.getGuardianId());
                meterRegistry.counter("fcm.failed.invalid_token").increment();
                return false;
            }
            case PERMANENT -> {
                // 예외 X -> 재시도 X
                log.error("FCM 영구 실패: guardianId={}, error={}",
                        guardian.getGuardianId(), result.errorMessage());
                meterRegistry.counter("fcm.failed.permanent").increment();
                return false;
            }
            default -> {
                return false;
            }
        }
    }
}