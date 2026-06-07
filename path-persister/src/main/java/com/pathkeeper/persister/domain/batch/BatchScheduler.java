package com.pathkeeper.persister.domain.batch;

import com.pathkeeper.persister.domain.location.LocationLogRecord;
import com.pathkeeper.persister.domain.location.LocationLogRepository;
import com.pathkeeper.persister.domain.stream.StreamAcker;
import com.pathkeeper.persister.domain.stream.StreamReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 배치 스케줄러.
 *
 * 매 10초마다:
 * 1. PEL의 미처리 메시지 먼저 처리
 * 2. 새 메시지 처리 (1000건 단위)
 * 3. PostgreSQL Bulk Insert
 * 4. ACK + DELETE
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BatchScheduler {

    private static final int BATCH_SIZE = 1000;
    private static final int MAX_BATCHES_PER_RUN = 10;   // 한 사이클에 최대 10000건

    private final StreamReader streamReader;
    private final LocationLogRepository repository;
    private final StreamAcker acker;
    private final BatchMetrics metrics;

    /** 동시 실행 방지 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Scheduled(fixedDelayString = "${persister.batch.interval-ms:10000}")
    public void process() {
        // 중복 실행 방지 (스케줄러 실행 시간이 interval보다 길어도 안전)
        if (!running.compareAndSet(false, true)) {
            log.debug("이전 배치 진행 중, 스킵");
            return;
        }

        try {
            // 1. PEL 먼저 비우기 (재처리)
            processPending();

            // 2. 새 메시지 처리
            processNew();

        } catch (Exception e) {
            log.error("배치 처리 실패", e);
            metrics.recordBatchError();
        } finally {
            running.set(false);
        }
    }

    /**
     * PEL의 미처리 메시지 처리.
     * 죽기 전에 받았던 메시지가 있으면 모두 처리.
     */
    private void processPending() {
        int totalProcessed = 0;

        for (int i = 0; i < MAX_BATCHES_PER_RUN; i++) {
            List<LocationLogRecord> records = streamReader.readPending(BATCH_SIZE);

            if (records.isEmpty()) {
                break;   // PEL 다 비웠음
            }

            int inserted = processBatch(records);
            totalProcessed += inserted;

            log.info("PEL 처리: batch={}, records={}, inserted={}",
                    i + 1, records.size(), inserted);
        }

        if (totalProcessed > 0) {
            log.info("PEL 전체 처리 완료: total={}", totalProcessed);
            metrics.recordPendingProcessed(totalProcessed);
        }
    }

    /**
     * 새 메시지 처리.
     * 한 사이클에 최대 MAX_BATCHES_PER_RUN개 배치만 처리.
     */
    private void processNew() {
        int totalProcessed = 0;

        for (int i = 0; i < MAX_BATCHES_PER_RUN; i++) {
            List<LocationLogRecord> records = streamReader.readNew(BATCH_SIZE);

            if (records.isEmpty()) {
                break;   // 새 메시지 없음
            }

            int inserted = processBatch(records);
            totalProcessed += inserted;

            // 배치가 다 안 차면 더 이상 없는 것 → 다음 사이클 대기
            if (records.size() < BATCH_SIZE) {
                break;
            }
        }

        if (totalProcessed > 0) {
            log.info("새 메시지 처리 완료: total={}", totalProcessed);
            metrics.recordNewProcessed(totalProcessed);
        }
    }

    /**
     * 한 배치 처리: Insert + ACK.
     * DB Insert(트랜잭션)가 성공한 후에만 ACK 수행.
     */
    private int processBatch(List<LocationLogRecord> records) {
        // 1. DB Bulk Insert (LocationLogRepository에 @Transactional 적용)
        int inserted = repository.bulkInsert(records);

        if (inserted == 0) {
            // INSERT 실패 → ACK 안 함 → 다음 사이클에 재시도
            log.warn("Insert 결과 0, ACK 스킵");
            return 0;
        }

        // 2. ACK + DELETE (트랜잭션 밖, Insert 커밋 후 실행)
        List<String> recordIds = records.stream()
                .map(LocationLogRecord::getRecordId)
                .toList();
        acker.ackAndDelete(recordIds);

        return inserted;
    }
}