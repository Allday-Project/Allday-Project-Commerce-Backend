package jpa.basic.alldayprojectcommerce.common.security.config;

import jpa.basic.alldayprojectcommerce.domain.keyword.service.KeywordCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisWarmUpRunner implements ApplicationRunner {

    private final KeywordCommandService keywordCommandService;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 서버 시작 시 자동 실행
     *
     * Redis 연결 가능한지 확인
     * 연결 불가 -> 서버 시작은 계속 진행
     * 연결 가능 -> 오늘 Redis 데이터가 있으면 Warm-up 스킵, 없으면 DB에서 복원
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("[Warm-up] 서버 시작 감지 - Redis 상태 확인");

        try {
            // Redis 연결 확인 — PING 대신 실제 작업으로 연결 상태 확인
            redisTemplate.hasKey("health-check");
        } catch (Exception e) {
            log.warn("[Warm-up] Redis 연결 불가 - 스킵 (서버는 정상 시작)");
            return;
        }

        try {
            keywordCommandService.warmUp();
        } catch (Exception e) {
            log.error("[Warm-up] 실패: {}", e.getMessage());
        }
    }
}
