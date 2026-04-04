package com.pathkeeper.backend.domain.user.service;

import com.pathkeeper.backend.domain.user.User;
import com.pathkeeper.backend.domain.user.dto.InviteCodeInfo;
import com.pathkeeper.backend.domain.user.dto.PartnerLinkInfo;
import com.pathkeeper.backend.domain.user.dto.ProfileInfo;
import com.pathkeeper.backend.domain.user.repository.UserRepository;
import com.pathkeeper.backend.global.exception.BusinessException;
import com.pathkeeper.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
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
    // Redis에 접근하기 위한 템플릿
    private final StringRedisTemplate redisTemplate;

    private static final String INVITE_CODE_PREFIX = "INVITE_CODE:";
    private static final long INVITE_CODE_EXPIRATION_MINUTES = 30;

    public ProfileInfo findProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return ProfileInfo.from(user);
    }

    public InviteCodeInfo generateInviteCode(String email) {
        String inviteCode = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String redisKey = INVITE_CODE_PREFIX + inviteCode;

        redisTemplate.opsForValue().set(redisKey, email, Duration.ofMinutes(INVITE_CODE_EXPIRATION_MINUTES));

        return new InviteCodeInfo(inviteCode);
    }

    @Transactional
    public PartnerLinkInfo linkPartner(String myEmail, String inviteCode) {
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

        me.linkPartner(partner);
        partner.linkPartner(me);

        redisTemplate.delete(redisKey);

        return new PartnerLinkInfo(partner.getId(), partner.getName());
    }

}
