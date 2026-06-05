package com.pathkeeper.api.domain.user.service;

import com.pathkeeper.api.domain.user.entity.GuardianRelation;
import com.pathkeeper.api.domain.user.entity.User;
import com.pathkeeper.api.domain.user.dto.InviteCodeResponse;
import com.pathkeeper.api.domain.user.dto.PartnerLinkResponse;
import com.pathkeeper.api.domain.user.dto.UserProfileResponse;
import com.pathkeeper.api.domain.user.repository.GuardianRelationRepository;
import com.pathkeeper.api.domain.user.repository.UserRepository;
import com.pathkeeper.api.global.exception.BusinessException;
import com.pathkeeper.api.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final GuardianRelationRepository guardianRelationRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String INVITE_CODE_PREFIX = "INVITE_CODE:";
    private static final long INVITE_CODE_EXPIRATION_MINUTES = 30;

    public UserProfileResponse findProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String partnerName = null;
        if (user.isGuardian()) {
            partnerName = guardianRelationRepository.findByGuardian(user)
                    .map(r -> r.getProtege().getName()).orElse(null);
        } else {
            partnerName = guardianRelationRepository.findByProtege(user)
                    .map(r -> r.getGuardian().getName()).orElse(null);
        }

        return new UserProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getRole().name(), partnerName);
    }

    public InviteCodeResponse generateInviteCode(String email) {
        String inviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String redisKey = INVITE_CODE_PREFIX + inviteCode;
        redisTemplate.opsForValue().set(redisKey, email, Duration.ofMinutes(INVITE_CODE_EXPIRATION_MINUTES));
        return new InviteCodeResponse(inviteCode);
    }

    @Transactional
    public PartnerLinkResponse linkPartner(String myEmail, String inviteCode) {
        String redisKey = INVITE_CODE_PREFIX + inviteCode.toUpperCase();
        String partnerEmail = redisTemplate.opsForValue().get(redisKey);

        if (partnerEmail == null)
            throw new BusinessException(ErrorCode.INVALID_INVITE_CODE);

        if (myEmail.equals(partnerEmail))
            throw new BusinessException(ErrorCode.CANNOT_LINK_SELF);

        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        User partner = userRepository.findByEmail(partnerEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        User guardian = me.isGuardian() ? me : partner;
        User protege = me.isProtege() ? me : partner;

        guardianRelationRepository.save(GuardianRelation.builder()
                .guardian(guardian)
                .protege(protege)
                .build());

        redisTemplate.delete(redisKey);

        return new PartnerLinkResponse(partner.getId(), partner.getName());
    }
}