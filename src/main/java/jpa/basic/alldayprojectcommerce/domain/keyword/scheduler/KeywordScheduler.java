package jpa.basic.alldayprojectcommerce.domain.keyword.scheduler;

import jpa.basic.alldayprojectcommerce.common.lock.repository.RedisLockRepository;
import jpa.basic.alldayprojectcommerce.domain.keyword.service.KeywordCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordScheduler {

    private final KeywordCommandService keywordCommandService;
    private final RedisLockRepository redisLockRepository;

    private static final String WRITE_BACK_LOCK_KEY = "lock:keyword:writeBack";
    private static final String MIDNIGHT_LOCK_KEY = "lock:keyword:midnightReset";
    private static final Long WRITE_BACK_LOCK_TTL = 55 * 60L; // 55분
    private static final Long MIDNIGHT_LOCK_TTL = 5 * 60L;    // 5분

    /**
     * Write-back 스케쥴러
     *
     * 매 1시간마다 실행
     * Redis ZSet -> SearchKeyword DB 동기화
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void writeBack() {
        log.info("[스케쥴러] Write-back 시작");

        String lockValue = UUID.randomUUID().toString();

        if (!redisLockRepository.tryLock(WRITE_BACK_LOCK_KEY, lockValue, WRITE_BACK_LOCK_TTL)) {
            log.info("[스케쥴러] Write-back 다른 서버 실행 중 - 스킵");
            return;
        }

        try {
            keywordCommandService.writeBack();
        } catch (Exception e) {
            // 스케쥴러가 실패해도 Redis는 건들지 않고 다음 주기에 재시도
            log.error("[스케쥴러] Write-back 실패", e);
        } finally {
            redisLockRepository.unlock(WRITE_BACK_LOCK_KEY, lockValue);
        }
    }

    /**
     * 자정 초기화 스케쥴러
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void midnightReset() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        log.info("[자정 초기화] 시작 - 기준 날짜: {}", yesterday);

        String lockValue = UUID.randomUUID().toString();

        if (!redisLockRepository.tryLock(MIDNIGHT_LOCK_KEY, lockValue, MIDNIGHT_LOCK_TTL)) {
            log.info("[자정 초기화] 다른 서버 실행 중 - 스킵");
            return;
        }

        try {
            // 마지막 Write-back
            keywordCommandService.writeBack(yesterday);

            // 어제 Top5 스냅샷 저장
            keywordCommandService.snapshotTop5(yesterday);

            // Redis 어제 데이터 초기화
            keywordCommandService.clearTodayRedisData(yesterday);

            // 오늘 Fallback Top5 생성
            keywordCommandService.saveFallbackTop5(today);

            log.info("[자정 초기화] 완료");
        } catch (Exception e) {
            // 실패 시 Redis 초기화 X
            log.error("[자정 초기화] 실패", e);
        } finally {
            redisLockRepository.unlock(MIDNIGHT_LOCK_KEY, lockValue);
        }
    }
}
