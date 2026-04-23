package jpa.basic.alldayprojectcommerce.common.util;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class RedisKeyUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // TTL - 자정 스케쥴러가 실패해도 다음날 삭제되도록 설계
    public static final Duration RANK_TTL = Duration.ofHours(25);

    // ZSet 키 - 날짜별 검색어 순위
    public String rankKey(LocalDate date) {
        return "search:rank:" + date.format(FORMATTER);
    }

    // 오늘 ZSet 키
    public String todayRankKey() {
        return rankKey(LocalDate.now());
    }

    // Set 키 - 날짜별 유저 검색 기록
    public String userLogKey(LocalDate date) {
        return "search:user:" + date.format(FORMATTER);
    }

    // 오늘 Set 키
    public String todayUserLogKey() {
        return userLogKey(LocalDate.now());
    }

    /**
     * 자정까지 남은 시간 + 1시간으로 계산
     * warm-up 시점에 TTL을 동적으로 계산할 때 사용
     */
    public Duration remainingTodayTtl() {
        LocalDateTime now      = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        Duration remaining     = Duration.between(now, midnight);

        // 자정까지 남은 시간 + 1시간
        return remaining.plusHours(1);
    }
}
