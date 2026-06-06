package com.pathkeeper.api.domain.location;

import com.pathkeeper.api.domain.location.dto.LocationRequest;
import com.pathkeeper.api.domain.user.entity.User;
import com.pathkeeper.api.domain.user.repository.UserRepository;
import com.pathkeeper.api.global.exception.BusinessException;
import com.pathkeeper.api.global.exception.ErrorCode;
import com.pathkeeper.common.dto.LocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationProducer locationProducer;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public void receiveLocation(String email, LocationRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateLocation(request);

        LocationMessage message = LocationMessage.of(
                user.getId(),
                request.lat(),
                request.lng(),
                request.batteryLevel(),
                request.recordedAt()
        );

        locationProducer.send(message);
    }

    private void validateLocation(LocationRequest request) {
        long now = System.currentTimeMillis();
        long recordedAt = request.recordedAt();

        if (recordedAt > now + 60_000) {
            throw new BusinessException(ErrorCode.INVALID_RECORDED_AT);
        }

        if (recordedAt < now - 60 * 60 * 1000) {
            throw new BusinessException(ErrorCode.INVALID_RECORDED_AT);
        }
    }
}