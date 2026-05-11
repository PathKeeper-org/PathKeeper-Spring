package com.pathkeeper.backend.domain.locationLog.repository;

import com.pathkeeper.backend.domain.locationLog.LocationLog;
import com.pathkeeper.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationLogRepository extends JpaRepository<LocationLog, Long> {

    Optional<LocationLog> findTopByUserOrderByRecordedAtDesc(User user);
}