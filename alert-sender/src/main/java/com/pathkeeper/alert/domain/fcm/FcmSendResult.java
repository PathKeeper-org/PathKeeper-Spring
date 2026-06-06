package com.pathkeeper.alert.domain.fcm;

/**
 * FCM 전송 결과
 */
public record FcmSendResult(
        boolean success,
        String messageId,
        FailureType failureType,
        String errorMessage
) {

    public static FcmSendResult success(String messageId) {
        return new FcmSendResult(true, messageId, null, null);
    }

    public static FcmSendResult retryable(String errorMessage) {
        return new FcmSendResult(false, null, FailureType.RETRYABLE, errorMessage);
    }

    public static FcmSendResult invalidToken(String errorMessage) {
        return new FcmSendResult(false, null, FailureType.INVALID_TOKEN, errorMessage);
    }

    public static FcmSendResult permanent(String errorMessage) {
        return new FcmSendResult(false, null, FailureType.PERMANENT, errorMessage);
    }

    public enum FailureType {
        /** 일시적 실패 (네트워크, 서비스 일시 장애) - 재시도 가능 */
        RETRYABLE,

        /** 토큰 무효 (등록 안 됨, 만료) - 토큰 삭제 후 종료 */
        INVALID_TOKEN,

        /** 영구적 실패 (메시지 형식 오류 등) - 즉시 DLQ */
        PERMANENT
    }
}