package com.pathkeeper.backend.domain.user;

import com.pathkeeper.backend.domain.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 보호자와 피보호자를 하나의 테이블로 관리하고, 초대 코드를 통해 1:1로 매핑
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // 암호화된 상태로 저장

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // GUARDIAN(보호자), PROTEGE(피보호자)

    @Column(unique = true, length = 6)
    private String inviteCode; // 6자리 초대 코드

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private User partner;

    @Column(length = 255)
    private String fcmToken; // 푸시 알림용 디바이스 토큰

    @Builder
    public User(String name, String email, String password, Role role, String inviteCode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.inviteCode = inviteCode;
    }

    // 비즈니스 로직: 파트너 연결
    public void linkPartner(User partner) {
        this.partner = partner;
    }

    // 비즈니스 로직: FCM 토큰 갱신
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}