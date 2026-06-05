package com.pathkeeper.api.domain.locationLog.repository;

import com.pathkeeper.api.domain.locationLog.entity.LocationLog;
import com.pathkeeper.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {

    Optional<LocationLog> findTopByUserOrderByRecordedAtDesc(User user);
}