package com.pathkeeper.api.domain.safezone.repository;

import com.pathkeeper.api.domain.safezone.entity.SafeZone;
import com.pathkeeper.api.domain.safezone.dto.SafeZoneProjection;
import com.pathkeeper.api.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SafeZoneRepository extends JpaRepository<SafeZone, Long> {
    void deleteByUser(User user);

    // ST_Buffer를 사용해 경로를 다각형으로 확장하여 저장
    @Modifying
    @Query(value = """
        INSERT INTO safe_zones (user_id, polygon, created_at, updated_at)
        VALUES (
            :userId,
            ST_Buffer(ST_GeomFromText(:wkt, 4326)::geography, :radius)::geography,
            NOW(),
            NOW()
        )
    """, nativeQuery = true)
    void saveSafeZoneWithBuffer(@Param("userId") Long userId,
                                @Param("wkt") String wkt,
                                @Param("radius") double radius
    );

    // 저장된 Polygon 데이터를 프론트엔드용 GeoJSON으로 변환하여 조회
    @Query(value = """
        SELECT safe_zone_id AS id, ST_AsGeoJSON(polygon) AS geoJson
        FROM safe_zones
        WHERE user_id = :userId
    """, nativeQuery = true)
    Optional<SafeZoneProjection> findSafeZoneInfoByUserId(@Param("userId") Long userId);

    // geography 타입은 ST_Contains 미지원 → geometry로 캐스트 후 포함 여부 판별
    @Query(value = """
        SELECT CASE WHEN ST_Contains(polygon::geometry, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)) THEN 1 ELSE 0 END
        FROM safe_zones
        WHERE user_id = :userId
    """, nativeQuery = true)
    Optional<Integer> isPointInSafeZone(@Param("userId") Long userId,
                                        @Param("lat") double lat,
                                        @Param("lng") double lng);
}
