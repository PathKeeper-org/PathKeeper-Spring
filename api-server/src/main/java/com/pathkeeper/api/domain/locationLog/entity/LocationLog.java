package com.pathkeeper.api.domain.locationLog.entity;

import com.pathkeeper.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
// ?분마다 피보호자의 위치를 기록
public class LocationLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "location_log_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // PostGIS Point 타입을 사용하여 위도/경도를 하나의 점으로 관리
    @Column(columnDefinition = "geometry(Point, 4326)", nullable = false)
    private Point location;

    // 배터리 잔량
    // 피보호자의 GPS가 갑자기 끊겼을 때, 배터리 때문인지 여부 파악 위함
    @Column(nullable = false)
    private int batteryLevel;

    @Column(nullable = false)
    private LocalDateTime recordedAt; // 안드로이드 단말기에서 실제로 위치를 측정한 시간

    // 이 테이블은 수정될 일이 없으므로 BaseTimeEntity 대신 CreatedDate만 따로 생성
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt; // 서버 DB에 인서트 된 시간

    @Builder
    public LocationLog(User user, Point location, int batteryLevel, LocalDateTime recordedAt) {
        this.user = user;
        this.location = location;
        this.batteryLevel = batteryLevel;
        this.recordedAt = recordedAt;
    }
}
