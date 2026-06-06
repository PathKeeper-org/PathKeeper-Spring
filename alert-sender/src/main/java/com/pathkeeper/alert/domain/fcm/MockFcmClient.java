package com.pathkeeper.alert.domain.fcm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 학습/테스트용 Mock FCM 클라이언트.
 * fcm.mock=true 일 때 활성화.
 *
 * 실제 FCM 호출 없이 결과를 시뮬레이션:
 * - 80% 확률로 성공
 * - 10% 확률로 retryable 실패 (테스트용)
 * - 10% 확률로 invalid token (테스트용)
 */
@Component
@ConditionalOnProperty(name = "fcm.mock", havingValue = "true")
@Slf4j
public class MockFcmClient implements FcmClient {

    @Override
    public FcmSendResult send(String token, String title, String body) {
        log.info("[MOCK FCM] 전송 시뮬레이션: token={}..., title={}",
                token.substring(0, Math.min(10, token.length())), title);

        // 실제 FCM 호출처럼 약간의 지연
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int dice = ThreadLocalRandom.current().nextInt(100);

        if (dice < 80) {
            return FcmSendResult.success("mock-msg-" + UUID.randomUUID());
        } else if (dice < 90) {
            return FcmSendResult.retryable("Mock: 일시적 실패 시뮬레이션");
        } else {
            return FcmSendResult.invalidToken("Mock: 잘못된 토큰 시뮬레이션");
        }
    }
}