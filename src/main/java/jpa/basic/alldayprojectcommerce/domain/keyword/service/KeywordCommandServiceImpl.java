package jpa.basic.alldayprojectcommerce.domain.keyword.service;

import jpa.basic.alldayprojectcommerce.common.util.RedisKeyUtils;
import jpa.basic.alldayprojectcommerce.domain.keyword.entity.SearchKeyword;
import jpa.basic.alldayprojectcommerce.domain.keyword.repository.PopularKeywordRepository;
import jpa.basic.alldayprojectcommerce.domain.keyword.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordCommandServiceImpl implements KeywordCommandService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SearchKeywordRepository searchKeywordRepository;
    private final PopularKeywordRepository popularKeywordRepository;

    // 한글, 영문, 숫자, 공백만 허용 (특수문자 전부 제거)
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[^가-힣a-zA-Z0-9\\s]");

    // 연속 공백을 1칸으로 축소하는 패턴
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");

    /**
     * 검색어 정규화
     *
     * "  아이폰  15   PRO!!! " 입력 시
     * 특수문자 제거   → "  아이폰  15   PRO "
     * 소문자 변환    → "  아이폰  15   pro "
     * 연속 공백 축소 → " 아이폰 15 pro "
     * 앞뒤 공백 제거 → "아이폰 15 pro"
     *
     * 결과: "아이폰 15 pro" 하나의 키워드로 처리
     */
    private String normalize(String query) {
        if (query == null ||  query.isEmpty()) return "";

        String result = SPECIAL_CHAR.matcher(query).replaceAll("");
        result = result.toLowerCase();
        result = MULTI_SPACE.matcher(result).replaceAll(" ");
        result = result.trim();

        return result;
    }

    /**
     * 회원 검색어 기록
     *
     * Redis Set memberValue: "user:{userId}:{keyword}"
     */
    @Override
    public void recordSearch(Long userId, String query) {
        String keyword = normalize(query);
        if (keyword.isEmpty() || keyword.length() < 2) return;

        String memberValue = "user:" + userId + ":" + keyword;
        doRecord(keyword, memberValue);
    }

    /**
     * 비회원 검색어 기록
     *
     * Redis Set memberValue: "ip:{ip}:{keyword}"
     */
    @Override
    public void recordSearchByIp(String ip, String query) {
        String keyword = normalize(query);
        if (keyword.isEmpty() || keyword.length() < 2) return;

        String memberValue = "ip:" + ip + ":" + keyword;
        doRecord(keyword, memberValue);
    }

    /**
     * Redis 기록 공통 로직
     *
     * SADD로 중복 체크 + 추가를 원자적으로 처리
     */
    public void doRecord(String keyword, String memberValue) {
        String rankKey    = RedisKeyUtils.todayRankKey();
        String userLogKey = RedisKeyUtils.todayUserLogKey();

        // SADD - 원자적 처리
        Long added = redisTemplate.opsForSet().add(userLogKey, memberValue);

        if (added == null || added == 0) {
            log.debug("[중복 검색 제외] keyword: {}", keyword);
            return;
        }

        redisTemplate.opsForZSet().incrementScore(rankKey, keyword, 1);
        log.debug("[검색어 기록] keyword: {}", keyword);
    }

    /**
     * Redis ZSet 전체 데이터를 꺼내서 SearchKeyword DB에 반영
     *
     * TypedTuple {value: "Allday", score: 10000.0} 형태
     * DB에 오늘 날짜로 해당 키워드가 있으면 searchCount 업데이트
     * DB에 없으면 새로 INSERT
     */
    @Override
    @Transactional
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
