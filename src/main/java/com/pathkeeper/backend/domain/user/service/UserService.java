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

    public ProfileInfo findProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(()-> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return ProfileInfo.from(user);
    }


}
