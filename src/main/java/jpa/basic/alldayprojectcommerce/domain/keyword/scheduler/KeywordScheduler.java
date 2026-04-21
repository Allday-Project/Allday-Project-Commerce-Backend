package jpa.basic.alldayprojectcommerce.domain.keyword.scheduler;

import jpa.basic.alldayprojectcommerce.domain.keyword.service.KeywordCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
}
