// api-server/src/main/java/com/pathkeeper/api/domain/safezone/SafeZone.java
package com.pathkeeper.api.domain.safezone.entity;

import com.pathkeeper.api.domain.user.entity.User;
import com.pathkeeper.api.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

@Entity
@Table(name = "safe_zones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SafeZone extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "safe_zone_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "geography(Polygon, 4326)", nullable = false)
    private Polygon polygon;

    // Bounding Box (Redis 캐시용 사전 계산 값)
    @Column(name = "bbox_min_lat", nullable = false)
    private Double bboxMinLat;

    @Column(name = "bbox_max_lat", nullable = false)
    private Double bboxMaxLat;

    @Column(name = "bbox_min_lng", nullable = false)
    private Double bboxMinLng;

    @Column(name = "bbox_max_lng", nullable = false)
    private Double bboxMaxLng;

    @Builder
    public SafeZone(User user, String name, Polygon polygon) {
        this.user = user;
        this.name = name;
        this.polygon = polygon;
        calculateBoundingBox();
    }

    /**
     * 다각형으로부터 Bounding Box 자동 계산
     */
    private void calculateBoundingBox() {
        org.locationtech.jts.geom.Envelope envelope = polygon.getEnvelopeInternal();
        this.bboxMinLat = envelope.getMinY();
        this.bboxMaxLat = envelope.getMaxY();
        this.bboxMinLng = envelope.getMinX();
        this.bboxMaxLng = envelope.getMaxX();
    }

    /**
     * 다각형 변경 시 Bounding Box 재계산
     */
    public void updatePolygon(Polygon newPolygon) {
        this.polygon = newPolygon;
        calculateBoundingBox();
    }

    /**
     * 이름 변경
     */
    public void updateName(String name) {
        this.name = name;
    }
}
