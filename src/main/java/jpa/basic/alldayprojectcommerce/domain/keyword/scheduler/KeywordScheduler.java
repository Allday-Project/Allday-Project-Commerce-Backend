package jpa.basic.alldayprojectcommerce.domain.keyword.scheduler;

import jpa.basic.alldayprojectcommerce.domain.keyword.service.KeywordCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeywordScheduler {

    private final KeywordCommandService keywordCommandService;

    /**
     * Write-back 스케쥴러
     *
     * 매 1시간마다 실행
     * Redis ZSet -> SearchKeyword DB 동기화
     */
    @Scheduled(fixedDelay = 60 * 60 * 1000)
    public void writeBack() {
        log.info("[스케쥴러] Write-back 시작");

        try {
            keywordCommandService.writeBack();
        } catch (Exception e) {
            // 스케쥴러가 실패해도 Redis는 건들지 않고 다음 주기에 재시도
            log.error("[스케쥴러] Write-back 실패: {}", e.getMessage());
        }
    }

    /**
     * 자정 초기화 스케쥴러
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void midnightReset() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        log.info("[자정 초기화] 시작 - 기준 날짜: {}", yesterday);

        try {
            // 마지막 Write-back
            keywordCommandService.writeBack();

            // 어제 Top5 스냅샷 저장
            keywordCommandService.snapshotTop5(yesterday);

            // Redis 어제 데이터 초기화
            keywordCommandService.clearTodayRedisData(yesterday);

            // 오늘 Fallback Top5 생성
            keywordCommandService.saveFallbackTop5(today);

            log.info("[자정 초기화] 완료");
        } catch (Exception e) {
            // 실패 시 Redis 초기화 X
            log.error("[자정 초기화] 실패: {}", e.getMessage());
        }
    }
}
