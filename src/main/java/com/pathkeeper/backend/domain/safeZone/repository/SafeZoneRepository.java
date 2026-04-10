package com.pathkeeper.backend.domain.safeZone.repository;

import com.pathkeeper.backend.domain.safeZone.SafeZone;
import com.pathkeeper.backend.domain.safeZone.dto.SafeZoneProjection;
import com.pathkeeper.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SafeZoneRepository extends JpaRepository<SafeZone, Long> {
    void deleteByUser(User user);

    // [중요] ST_Buffer를 사용해 경로를 다각형으로 확장하여 저장
    @Modifying
    @Query(value = """
        INSERT INTO safe_zone (user_id, polygon, created_at, updated_at)
        VALUES (
            :userId, 
            ST_Buffer(ST_GeomFromText(:wkt, 4326)::geography, :radius)::geometry,
            NOW(),
            NOW()
        )
    """, nativeQuery = true)
    void saveSafeZoneWithBuffer(@Param("userId") Long userId,
                                @Param("wkt") String wkt,
                                @Param("radius") double radius
    );

    // [중요] 저장된 Polygon 데이터를 프론트엔드용 GeoJSON으로 변환하여 조회
    @Query(value = """
        SELECT safe_zone_id AS id, ST_AsGeoJSON(polygon) AS geoJson
        FROM safe_zone 
        WHERE user_id = :userId
    """, nativeQuery = true)
    Optional<SafeZoneProjection> findSafeZoneInfoByUserId(@Param("userId") Long userId);
}
