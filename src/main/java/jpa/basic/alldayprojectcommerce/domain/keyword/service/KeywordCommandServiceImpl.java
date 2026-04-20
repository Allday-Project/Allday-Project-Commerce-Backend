package jpa.basic.alldayprojectcommerce.domain.keyword.service;

import jpa.basic.alldayprojectcommerce.common.util.RedisKeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordCommandServiceImpl implements KeywordCommandService {

    private final RedisTemplate<String, String> redisTemplate;

    // 한글, 영문, 숫자, 공백만 허용 (특수문자 전부 제거)
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[^가-힣a-zA-Z0-9\\s]");

    /**
     * 검색어 기록
     *
     * 1. 특수문자 제거 + 공백 기준 단어 파싱
     * 2. 각 단어를 Redis ZSet에 ZINCRBY로 +1
     */
    @Override
    public void createRecordSearch(Long loginId, String query) {
        // 빈 검색어 무시
        if (query == null || query.isBlank()) {
            return;
        }

        // 특수문자 제거 후 앞뒤 공백 제거
        String cleaned = SPECIAL_CHAR.matcher(query.trim()).replaceAll("");

        // 공백 기준으로 단어 분리
        String[] keywords = cleaned.split("\\s+");

        /**
         * Redis ZSet 키 형식: "search:rank:<현재 날짜>"
         * 스케쥴러가 특정 날짜 키만 골라서 삭제할 수 있도록 설계
         */
        String rankKey = RedisKeyUtils.todayRankKey();
        /**
         * Redis Set 키 형식: "search:user:<현재 날짜>"
         * 고객의 특정 날짜 중복 검색 여부 확인
         */
        String userLogKey = RedisKeyUtils.todayUserLogKey();

        for (String keyword : keywords) {
            // 1글자 이하는 의미없는 검색어로 간주하고 제외
            if (keyword.isBlank() || keyword.length() < 2) {
                continue;
            }

            // "userId:keyword" 조합으로 오늘 이 고객이 이 키워드를 검색했는지 확인
            String memberValue = loginId + ":" + keyword;

            /**
             * SADD - 추가 + 중복 여부 확인을 원자적으로 한 번에 처리
             * 반환값: 새로 추가(1), 이미 존재(0)
             */
            Long added = redisTemplate.opsForSet().add(userLogKey, memberValue);

            if (added == null || added == 0) {
                log.debug("[중복 검색 제외] userId: {}, keyword: {}", loginId, keyword);
                continue;
            }

            // 처음 검색한 키워드만 ZSet score + 1
            redisTemplate.opsForZSet().incrementScore(rankKey, keyword, 1);
            log.debug("[검색어 기록] userId: {}, keyword: {}", loginId, keyword);
        }
    }
}
