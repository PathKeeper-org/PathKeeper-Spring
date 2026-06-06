// path-persister/src/main/java/com/pathkeeper/persister/domain/location/LocationLog.java
package com.pathkeeper.persister.domain.location;

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
@IdClass(LocationLogId.class)
public class LocationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_log_seq")
    @SequenceGenerator(
            name = "location_log_seq",
            sequenceName = "location_logs_location_log_id_seq",  // BIGSERIAL이 생성한 시퀀스명
            allocationSize = 1000  // batch_size와 동일하게 설정하여 ID를 미리 선점
    )
    @Column(name = "location_log_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(columnDefinition = "geography(Point, 4326)", nullable = false)
    private Point location;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Id
    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public LocationLog(Long userId, Point location, Double lat, Double lng,
                       Integer batteryLevel, LocalDateTime recordedAt) {
        this.userId = userId;
        this.location = location;
        this.lat = lat;
        this.lng = lng;
        this.batteryLevel = batteryLevel;
        this.recordedAt = recordedAt;
    }
}