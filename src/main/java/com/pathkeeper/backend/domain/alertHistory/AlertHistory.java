package com.pathkeeper.backend.domain.alertHistory;

import com.pathkeeper.backend.domain.common.BaseEntity;
import com.pathkeeper.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlertHistory extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_history_id")
    private Long id;

    // 알림을 받을 사람 (보호자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlertType type; // ZONE_OUT(이탈), LOW_BATTERY(배터리 부족), SOS(긴급 호출) 등

    @Column(nullable = false)
    private String title; // 푸시 알림 제목 (예: "안심존 이탈 알림")

    @Column(nullable = false)
    private String message; // 푸시 알림 내용 (예: "경로를 벗어났습니다.")

//    @Column(nullable = false)
//    private boolean isRead; // 보호자가 이 알림을 읽었는지 여부

//    @Builder
//    public AlertHistory(User user, AlertType type, String title, String message) {
//        this.user = user;
//        this.type = type;
//        this.title = title;
//        this.message = message;
//        this.isRead = false;
//    }

    @Builder
    public AlertHistory(User user, AlertType type, String title, String message) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
    }
}
