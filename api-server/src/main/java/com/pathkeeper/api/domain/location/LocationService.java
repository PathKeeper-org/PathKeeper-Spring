package com.pathkeeper.api.domain.location;

import com.pathkeeper.api.domain.location.dto.LocationRequest;
import com.pathkeeper.common.dto.LocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationProducer locationProducer;

    /**
     * 위치 데이터를 받아 Kafka로 발행한다.
     */
    public void receiveLocation(Long userId, LocationRequest request) {
        // 1. 추가 검증 (필요 시)
        validateLocation(request);

        // 2. DTO → 메시지 변환
        LocationMessage message = LocationMessage.of(
                userId,
                request.lat(),
                request.lng(),
                request.batteryLevel(),
                request.recordedAt()
        );

        // 3. Kafka 발행
        locationProducer.send(message);
    }

    /**
     * 비즈니스 검증.
     * @Valid로 안 되는 추가 검증은 여기서.
     */
    private void validateLocation(LocationRequest request) {
        long now = System.currentTimeMillis();
        long recordedAt = request.recordedAt();

        // 미래 시각 거부 (1분 이상 차이 시)
        if (recordedAt > now + 60_000) {
            throw new IllegalArgumentException(
                    "측정 시각이 미래입니다: recordedAt=" + recordedAt
            );
        }

        // 너무 오래된 데이터 거부 (1시간 이상 지난 데이터)
        if (recordedAt < now - 60 * 60 * 1000) {
            throw new IllegalArgumentException(
                    "측정 시각이 너무 오래되었습니다: recordedAt=" + recordedAt
            );
        }
    }
}
