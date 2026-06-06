package com.pathkeeper.alert.domain.fcm;

public interface FcmClient {

    /**
     * FCM 메시지 전송
     */
    FcmSendResult send(String token, String title, String body);
}