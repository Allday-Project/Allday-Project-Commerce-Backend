package jpa.basic.alldayprojectcommerce.common.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class RedisKeyUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

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
}
