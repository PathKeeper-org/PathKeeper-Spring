// api-server/src/main/java/com/pathkeeper/api/domain/departure/DepartureEvent.java
package com.pathkeeper.api.domain.departure.entity;

import com.pathkeeper.api.domain.safezone.entity.SafeZone;
import com.pathkeeper.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "departure_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class DepartureEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "departure_event_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "safe_zone_id")
    private SafeZone safeZone;

    @Column(name = "departed_lat", nullable = false)
    private Double departedLat;

    @Column(name = "departed_lng", nullable = false)
    private Double departedLng;

    @Column(name = "departed_at", nullable = false)
    private LocalDateTime departedAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(nullable = false)
    private Boolean notified;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public DepartureEvent(User user, SafeZone safeZone,
                          Double departedLat, Double departedLng,
                          LocalDateTime departedAt) {
        this.user = user;
        this.safeZone = safeZone;
        this.departedLat = departedLat;
        this.departedLng = departedLng;
        this.departedAt = departedAt;
        this.notified = false;
    }

    /**
     * 알림 발송 완료 기록
     */
    public void markNotified() {
        this.notified = true;
        this.notifiedAt = LocalDateTime.now();
    }

    /**
     * 복귀 시각 기록
     */
    public void markReturned(LocalDateTime returnedAt) {
        this.returnedAt = returnedAt;
    }

    public boolean isReturned() {
        return returnedAt != null;
    }
}
