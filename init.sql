-- =============================================================================
-- PathKeeper Database Schema
-- PostgreSQL 15 + PostGIS 3.3
-- =============================================================================

-- PostGIS 확장 활성화
CREATE EXTENSION IF NOT EXISTS postgis;

-- =============================================================================
-- 1. users : 통합 사용자 테이블 (보호자/피보호자)
-- =============================================================================
CREATE TABLE IF NOT EXISTS users (
                                     user_id          BIGSERIAL PRIMARY KEY,
                                     name             VARCHAR(50) NOT NULL,
    email            VARCHAR(100) NOT NULL UNIQUE,
    password         VARCHAR(255) NOT NULL,                  -- BCrypt 해시
    role             VARCHAR(20) NOT NULL,                   -- 'GUARDIAN' or 'PROTEGE'
    fcm_token        VARCHAR(500),

    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_users_role CHECK (role IN ('GUARDIAN', 'PROTEGE'))
    );

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- =============================================================================
-- 2. guardian_relations : 보호자 ↔ 피보호자 매핑 (N:N)
-- =============================================================================
CREATE TABLE IF NOT EXISTS guardian_relations (
                                                  relation_id      BIGSERIAL PRIMARY KEY,
                                                  guardian_id      BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    protege_id       BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,

    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_guardian_protege UNIQUE (guardian_id, protege_id),
    CONSTRAINT chk_different_users CHECK (guardian_id != protege_id)
    );

CREATE INDEX idx_guardian_relations_guardian ON guardian_relations(guardian_id);
CREATE INDEX idx_guardian_relations_protege ON guardian_relations(protege_id);

-- =============================================================================
-- 3. safe_zones : 안심존 다각형
-- =============================================================================
CREATE TABLE IF NOT EXISTS safe_zones (
                                          safe_zone_id     BIGSERIAL PRIMARY KEY,
                                          user_id          BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    name             VARCHAR(100) NOT NULL,

    -- 다각형 (PostGIS GEOGRAPHY: 구면 좌표계, 미터 단위 정확)
    polygon          GEOGRAPHY(POLYGON, 4326) NOT NULL,

    -- Bounding Box 캐시 (Redis 1차 필터링용, 저장 시 자동 계산)
    bbox_min_lat     DOUBLE PRECISION NOT NULL,
    bbox_max_lat     DOUBLE PRECISION NOT NULL,
    bbox_min_lng     DOUBLE PRECISION NOT NULL,
    bbox_max_lng     DOUBLE PRECISION NOT NULL,

    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX idx_safe_zones_user_id ON safe_zones(user_id);

-- =============================================================================
-- 4. location_logs : 위치 이력 (월별 파티션)
-- =============================================================================
-- 파티션 마스터 테이블
CREATE TABLE IF NOT EXISTS location_logs (
                                             location_log_id  BIGSERIAL,
                                             user_id          BIGINT NOT NULL,

    -- 위치 (PostGIS POINT, SRID 4326)
                                             location         GEOGRAPHY(POINT, 4326) NOT NULL,

    -- 단순 위경도 (인덱스/조회 편의)
    lat              DOUBLE PRECISION NOT NULL,
    lng              DOUBLE PRECISION NOT NULL,

    battery_level    SMALLINT,                          -- 0~100, NULL 허용
    recorded_at      TIMESTAMP NOT NULL,                -- 클라이언트 측정 시각
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),  -- 서버 적재 시각

    PRIMARY KEY (location_log_id, recorded_at),

    CONSTRAINT chk_battery_level CHECK (
                                           battery_level IS NULL OR (battery_level BETWEEN 0 AND 100)
    )
    ) PARTITION BY RANGE (recorded_at);

-- 파티션 생성 (학습용 3개월치 미리 생성)
CREATE TABLE IF NOT EXISTS location_logs_2026_05 PARTITION OF location_logs
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

CREATE TABLE IF NOT EXISTS location_logs_2026_06 PARTITION OF location_logs
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');

CREATE TABLE IF NOT EXISTS location_logs_2026_07 PARTITION OF location_logs
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');

CREATE TABLE IF NOT EXISTS location_logs_2026_08 PARTITION OF location_logs
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');

-- 파티션별 인덱스 (각 파티션에 자동 생성)
CREATE INDEX idx_location_logs_user_recorded
    ON location_logs(user_id, recorded_at DESC);

-- Hibernate SEQUENCE 전략의 allocationSize(1000)와 일치시켜 배치 INSERT 최적화
ALTER SEQUENCE location_logs_location_log_id_seq INCREMENT BY 1000;

-- =============================================================================
-- 5. departure_events : 이탈 이벤트 이력
-- =============================================================================
CREATE TABLE IF NOT EXISTS departure_events (
                                                departure_event_id  BIGSERIAL PRIMARY KEY,
                                                user_id             BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    safe_zone_id        BIGINT REFERENCES safe_zones(safe_zone_id) ON DELETE SET NULL,

    -- 이탈 시점의 위치
    departed_lat        DOUBLE PRECISION NOT NULL,
    departed_lng        DOUBLE PRECISION NOT NULL,
    departed_at         TIMESTAMP NOT NULL,

    -- 복귀 정보 (NULL이면 아직 복귀 안 함)
    returned_at         TIMESTAMP,

    -- 알림 발송 추적
    notified            BOOLEAN NOT NULL DEFAULT FALSE,
    notified_at         TIMESTAMP,

    created_at          TIMESTAMP NOT NULL DEFAULT NOW()
    );

CREATE INDEX idx_departure_events_user_departed ON departure_events(user_id, departed_at DESC);

-- =============================================================================
-- 6. updated_at 자동 갱신 트리거
-- =============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_safe_zones_updated_at
    BEFORE UPDATE ON safe_zones
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =============================================================================
-- 검증 메시지
-- =============================================================================
DO $$
BEGIN
    RAISE NOTICE 'PathKeeper schema initialized successfully';
    RAISE NOTICE 'PostGIS version: %', PostGIS_Version();
END $$;