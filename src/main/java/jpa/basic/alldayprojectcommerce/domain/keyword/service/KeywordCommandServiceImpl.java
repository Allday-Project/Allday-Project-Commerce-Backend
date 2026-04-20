package jpa.basic.alldayprojectcommerce.domain.keyword.service;

import jpa.basic.alldayprojectcommerce.common.util.RedisKeyUtils;
import jpa.basic.alldayprojectcommerce.domain.keyword.entity.SearchKeyword;
import jpa.basic.alldayprojectcommerce.domain.keyword.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordCommandServiceImpl implements KeywordCommandService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SearchKeywordRepository searchKeywordRepository;

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

    /**
     * Redis ZSet 전체 데이터를 꺼내서 SearchKeyword DB에 반영
     *
     * TypedTuple {value: "Allday", score: 10000.0} 형태
     * DB에 오늘 날짜로 해당 키워드가 있으면 searchCount 업데이트
     * DB에 없으면 새로 INSERT
     */
    @Override
    public void writeBack() {
        LocalDate today = LocalDate.now();
        String rankKey = RedisKeyUtils.todayRankKey();

        Set<ZSetOperations.TypedTuple<String>> allData =
                redisTemplate.opsForZSet().rangeWithScores(rankKey, 0, -1);

        if (allData == null || allData.isEmpty()) {
            log.info("[Write-back] 동기화할 데이터 없음");
            return;
        }

        for (ZSetOperations.TypedTuple<String> tuple : allData) {
            String keyword = tuple.getValue();
            long count = tuple.getScore() == null ? 0L : tuple.getScore().longValue();

            if (keyword == null || keyword.isBlank()) continue;

            // 오늘 날짜 + 키워드로 기존 레코드 조회
            searchKeywordRepository
                    .findByKeywordAndSearchDate(keyword, today)
                    .ifPresentOrElse(
                            existing -> {
                                /**
                                 * 이미 있으면 Redis 현재값으로 덮어씌우기
                                 * Redis에는 오늘 누적 전체가 담겨있어서 그냥 덮어쓰기
                                 */
                                existing.setCount(count);
                            },
                            () -> {
                                // 없으면 새로 INSERT
                                searchKeywordRepository.save(
                                        SearchKeyword.builder()
                                                .keyword(keyword)
                                                .searchCount(count)
                                                .searchDate(today)
                                                .build()
                                );
                            }
                    );
        }

        log.info("[Write-back] {}건 DB 동기화 완료", allData.size());
    }
}
