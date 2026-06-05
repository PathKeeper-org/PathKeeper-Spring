package com.pathkeeper.api.domain.user.service;

import com.pathkeeper.api.domain.user.repository.GuardianRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GuardianRelationService {
    private final GuardianRelationRepository guardianRelationRepository;


}
