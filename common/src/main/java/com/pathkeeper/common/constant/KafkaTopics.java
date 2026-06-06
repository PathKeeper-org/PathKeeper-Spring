package com.pathkeeper.common.constant;

/**
 * Kafka 토픽 이름 상수.
 * 모든 서비스가 같은 토픽 이름을 참조하도록 중앙화.
 */
public final class KafkaTopics {

    public static final String LOCATION_EVENTS = "location-events";
    public static final String LOCATION_EVENTS_DLQ = "location-events.DLQ";
    public static final String ALERT_EVENTS = "alert-events";
    public static final String ALERT_EVENTS_DLQ = "alert-events.DLQ";

    private KafkaTopics() {
        throw new UnsupportedOperationException("상수 클래스는 인스턴스화할 수 없습니다");
    }
}
