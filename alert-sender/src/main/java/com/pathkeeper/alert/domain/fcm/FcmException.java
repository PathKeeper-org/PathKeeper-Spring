package com.pathkeeper.alert.domain.fcm;

/**
 * 재시도 가능한 FCM 실패.
 * 이 예외를 던지면 ErrorHandler가 재시도 → DLQ 처리.
 */
public class FcmException extends RuntimeException {

    public FcmException(String message) {
        super(message);
    }

    public FcmException(String message, Throwable cause) {
        super(message, cause);
    }
}
