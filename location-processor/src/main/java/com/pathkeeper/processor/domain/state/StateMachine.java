// StateMachine.java
package com.pathkeeper.processor.domain.state;

import com.pathkeeper.common.dto.LocationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 상태 머신 + 히스테리시스.
 * GPS 노이즈에 견고하게 상태 전이를 판정.
 *
 * 전이 조건:
 * - INSIDE → OUTSIDE: 연속 N(3)번 OUTSIDE
 * - OUTSIDE → INSIDE: 연속 M(2)번 INSIDE
 *
 * 중간에 반대 결과가 나오면 카운터 리셋.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StateMachine {

    private static final String STATE_KEY_PREFIX = "user:state:";
    private static final Duration STATE_TTL = Duration.ofDays(30);

    /** INSIDE → OUTSIDE 확정 임계값 */
    private static final int EXIT_THRESHOLD = 3;

    /** OUTSIDE → INSIDE 확정 임계값 */
    private static final int RETURN_THRESHOLD = 2;

    /** 카운터 리셋 윈도우 (5분) */
    private static final long RESET_WINDOW_MS = 5 * 60 * 1000;

    private final StringRedisTemplate redis;

    /**
     * 현재 좌표와 isInside 결과를 받아 상태 전이를 평가하고 Redis에 저장.
     */
    public StateTransition evaluate(LocationMessage message, boolean currentlyInside) {
        String key = STATE_KEY_PREFIX + message.userId();
        Map<Object, Object> stateData = redis.opsForHash().entries(key);

        // 신규 사용자 처리
        if (stateData.isEmpty()) {
            saveState(key, UserState.initial(
                    currentlyInside, message.lat(), message.lng(), message.publishedAt()
            ));
            log.info("신규 사용자 상태 초기화: userId={}, status={}",
                    message.userId(), currentlyInside ? "INSIDE" : "OUTSIDE");
            return StateTransition.INITIALIZED;
        }

        // 기존 상태 파싱
        String prevStatus = (String) stateData.get("status");
        int exitCount = parseInt(stateData.get("exitSuspicionCount"));
        int returnCount = parseInt(stateData.get("returnSuspicionCount"));
        long lastUpdate = parseLong(stateData.get("lastUpdate"));

        // 장기 휴면 후 메시지: 카운터 리셋
        if (message.publishedAt() - lastUpdate > RESET_WINDOW_MS) {
            log.debug("장기 휴면 후 메시지, 카운터 리셋: userId={}", message.userId());
            exitCount = 0;
            returnCount = 0;
        }

        StateTransition transition;
        String newStatus = prevStatus;

        if ("INSIDE".equals(prevStatus)) {
            // INSIDE 상태에서의 평가
            if (currentlyInside) {
                exitCount = 0;
                transition = StateTransition.NO_CHANGE;
            } else {
                exitCount++;
                if (exitCount >= EXIT_THRESHOLD) {
                    transition = StateTransition.INSIDE_TO_OUTSIDE;
                    newStatus = "OUTSIDE";
                    exitCount = 0;
                    log.info("이탈 확정: userId={}, threshold={}",
                            message.userId(), EXIT_THRESHOLD);
                } else {
                    transition = StateTransition.PENDING;
                    log.debug("이탈 의심: userId={}, count={}/{}",
                            message.userId(), exitCount, EXIT_THRESHOLD);
                }
            }
        } else {
            // OUTSIDE 상태에서의 평가
            if (!currentlyInside) {
                returnCount = 0;
                transition = StateTransition.NO_CHANGE;
            } else {
                returnCount++;
                if (returnCount >= RETURN_THRESHOLD) {
                    transition = StateTransition.OUTSIDE_TO_INSIDE;
                    newStatus = "INSIDE";
                    returnCount = 0;
                    log.info("복귀 확정: userId={}", message.userId());
                } else {
                    transition = StateTransition.PENDING;
                    log.debug("복귀 의심: userId={}, count={}/{}",
                            message.userId(), returnCount, RETURN_THRESHOLD);
                }
            }
        }

        // 상태 저장
        saveState(key, UserState.builder()
                .status(newStatus)
                .exitSuspicionCount(exitCount)
                .returnSuspicionCount(returnCount)
                .lastLat(message.lat())
                .lastLng(message.lng())
                .lastUpdate(message.publishedAt())
                .build());

        return transition;
    }

    private void saveState(String key, UserState state) {
        Map<String, String> fields = new HashMap<>();
        fields.put("status", state.getStatus());
        fields.put("exitSuspicionCount", String.valueOf(state.getExitSuspicionCount()));
        fields.put("returnSuspicionCount", String.valueOf(state.getReturnSuspicionCount()));
        fields.put("lastLat", String.valueOf(state.getLastLat()));
        fields.put("lastLng", String.valueOf(state.getLastLng()));
        fields.put("lastUpdate", String.valueOf(state.getLastUpdate()));

        redis.opsForHash().putAll(key, fields);
        redis.expire(key, STATE_TTL);
    }

    private int parseInt(Object value) {
        if (value == null) return 0;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private long parseLong(Object value) {
        if (value == null) return 0L;
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}