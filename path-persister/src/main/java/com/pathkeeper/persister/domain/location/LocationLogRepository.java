package com.pathkeeper.persister.domain.location;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * LocationLog Bulk Insert 전용 Repository.
 *
 * JPA 대신 JDBC를 사용하는 이유:
 * - 1000건 일괄 INSERT 시 JDBC가 훨씬 빠름
 * - JPA는 영속성 컨텍스트 관리 오버헤드
 * - 명시적 SQL로 PostGIS 함수 호출 가능
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class LocationLogRepository {

    private static final String INSERT_SQL = """
        INSERT INTO location_logs 
            (user_id, location, lat, lng, battery_level, recorded_at, created_at)
        VALUES (
            ?, 
            ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
            ?, ?, ?, ?, NOW()
        )
        """;

    private final JdbcTemplate jdbcTemplate;

    /**
     * 위치 로그 일괄 저장.
     *
     * @return 실제 INSERT된 row 수
     */
    @Transactional
    public int bulkInsert(List<LocationLogRecord> records) {
        if (records.isEmpty()) {
            return 0;
        }

        long startTime = System.currentTimeMillis();

        int[] result = jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                LocationLogRecord record = records.get(i);

                ps.setLong(1, record.getUserId());
                ps.setDouble(2, record.getLng());     // ST_MakePoint(lng, lat)
                ps.setDouble(3, record.getLat());
                ps.setDouble(4, record.getLat());     // 별도 lat 컬럼
                ps.setDouble(5, record.getLng());     // 별도 lng 컬럼

                if (record.getBatteryLevel() != null) {
                    ps.setInt(6, record.getBatteryLevel());
                } else {
                    ps.setNull(6, java.sql.Types.SMALLINT);
                }

                LocalDateTime recordedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(record.getRecordedAtMilli()),
                        ZoneId.of("Asia/Seoul")
                );
                ps.setTimestamp(7, Timestamp.valueOf(recordedAt));
            }

            @Override
            public int getBatchSize() {
                return records.size();
            }
        });

        long elapsed = System.currentTimeMillis() - startTime;
        int totalInserted = (int) java.util.Arrays.stream(result).filter(r -> r > 0).count();

        log.info("Bulk insert 완료: count={}, elapsed={}ms, tps={}",
                totalInserted, elapsed,
                elapsed > 0 ? (totalInserted * 1000L / elapsed) : 0);

        return totalInserted;
    }
}