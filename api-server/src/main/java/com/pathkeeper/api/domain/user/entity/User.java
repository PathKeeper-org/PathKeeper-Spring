// api-server/src/main/java/com/pathkeeper/api/domain/user/User.java
package com.pathkeeper.api.domain.user.entity;

import com.pathkeeper.api.global.entity.BaseEntity;
import com.pathkeeper.common.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "fcm_token", length = 500)
    private String fcmToken;

    @Builder
    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    /**
     * FCM 토큰 갱신
     */
    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * 비밀번호 변경 (이미 암호화된 값 전달)
     */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    /**
     * 보호자인지 확인
     */
    public boolean isGuardian() {
        return this.role == Role.GUARDIAN;
    }

    /**
     * 피보호자인지 확인
     */
    public boolean isProtege() {
        return this.role == Role.PROTEGE;
    }
}