package com.pathkeeper.alert.domain.fcm;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Firebase Admin SDK 기반 FCM 클라이언트.
 * fcm.mock=false (또는 미설정) 일 때 활성화.
 */
@Component
@ConditionalOnProperty(name = "fcm.mock", havingValue = "false", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class FirebaseFcmClient implements FcmClient {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public FcmSendResult send(String token, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("type", "departure")
                .build();

        try {
            String messageId = firebaseMessaging.send(message);
            log.debug("FCM 전송 성공: messageId={}", messageId);
            return FcmSendResult.success(messageId);

        } catch (FirebaseMessagingException e) {
            return handleFirebaseException(e);

        } catch (Exception e) {
            log.error("FCM 전송 예외", e);
            return FcmSendResult.retryable(e.getMessage());
        }
    }

    private FcmSendResult handleFirebaseException(FirebaseMessagingException e) {
        MessagingErrorCode errorCode = e.getMessagingErrorCode();

        if (errorCode == null) {
            return FcmSendResult.retryable("Unknown error: " + e.getMessage());
        }

        return switch (errorCode) {
            // 토큰 관련 (영구적, 토큰 삭제 필요)
            case UNREGISTERED, INVALID_ARGUMENT, SENDER_ID_MISMATCH ->
                    FcmSendResult.invalidToken(errorCode.name() + ": " + e.getMessage());

            // 일시적 (재시도 가능)
            case UNAVAILABLE, INTERNAL, QUOTA_EXCEEDED ->
                    FcmSendResult.retryable(errorCode.name() + ": " + e.getMessage());

            // 메시지 형식 오류 등 (영구적, 재시도 무의미)
            case THIRD_PARTY_AUTH_ERROR ->
                    FcmSendResult.permanent(errorCode.name() + ": " + e.getMessage());

            default ->
                    FcmSendResult.retryable(errorCode.name() + ": " + e.getMessage());
        };
    }
}