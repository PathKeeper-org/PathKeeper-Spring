package com.pathkeeper.api.domain.user.repository;

import com.pathkeeper.api.domain.user.entity.GuardianRelation;
import com.pathkeeper.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuardianRelationRepository extends JpaRepository<GuardianRelation, Long> {
    Optional<GuardianRelation> findByGuardian(User guardian);
    Optional<GuardianRelation> findByProtege(User protege);
}