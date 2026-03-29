package com.pathkeeper.backend.domain.safeZone;

import com.pathkeeper.backend.domain.common.BaseEntity;
import com.pathkeeper.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon; // JTS 라이브러리 사용

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// T MAP을 통해 그려질 다각형(Polygon) 데이터를 저장
// PostgreSQL의 PostGIS 확장을 활용해 진짜 공간 데이터로 저장
public class SafeZone extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "safe_zone_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user; // 안심존의 주인 (피보호자)

    // PostGIS의 geometry 타입을 사용하여 다각형 좌표를 통째로 저장 (SRID 4326 = GPS 좌표계)
    @Column(columnDefinition = "geometry(Polygon, 4326)", nullable = false)
    private Polygon polygon;

//    @Column(nullable = false)
//    private boolean isActive;

//    @Builder
//    public SafeZone(User user, Polygon polygon, Boolean isActive) {
//        this.user = user;
//        this.polygon = polygon;
//        this.isActive = isActive;
//    }

    @Builder
    public SafeZone(User user, Polygon polygon) {
        this.user = user;
        this.polygon = polygon;
    }
}
