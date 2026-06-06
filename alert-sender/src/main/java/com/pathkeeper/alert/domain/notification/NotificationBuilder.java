package com.pathkeeper.alert.domain.notification;

import com.pathkeeper.common.dto.AlertEvent;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * AlertEvent로부터 FCM 알림 텍스트 생성.
 */
@Component
public class NotificationBuilder {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    // 서울 시간대를 상수로 선언하여 딱 한 번만 만들고 재사용
    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    public Notification build(AlertEvent event, String protegeName) {
        return switch (event.type()) {
            case DEPARTURE -> buildDepartureNotification(event, protegeName);
            case RETURN -> buildReturnNotification(event, protegeName);
        };
    }

    private Notification buildDepartureNotification(AlertEvent event, String protegeName) {
        String time = formatTime(event.occurredAt());

        String title = "안심존 이탈 알림";
        String body = String.format("%s님이 %s에 안심존을 벗어났습니다", protegeName, time);

        return new Notification(title, body);
    }

    private Notification buildReturnNotification(AlertEvent event, String protegeName) {
        String time = formatTime(event.occurredAt());

        String title = "안심존 복귀 알림";
        String body = String.format("%s님이 %s에 안심존으로 돌아왔습니다", protegeName, time);

        return new Notification(title, body);
    }

    private String formatTime(long epochMilli) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMilli),
                SEOUL_ZONE
        ).format(TIME_FORMATTER);
    }

    public record Notification(String title, String body) {}
}