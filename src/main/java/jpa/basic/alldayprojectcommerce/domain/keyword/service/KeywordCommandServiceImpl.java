package jpa.basic.alldayprojectcommerce.domain.keyword.service;

import jpa.basic.alldayprojectcommerce.common.util.RedisKeyUtils;
import jpa.basic.alldayprojectcommerce.domain.keyword.entity.PopularKeyword;
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
import java.util.List;
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

    /**
     * Top5 스냅샷 저장
     *
     * SearchKeyword에서 꺼내서 PopularKeyword에 순위별로 저장
     * isFallback = false -> Top5 순위권
     */
    @Override
    @Transactional
    public void snapshotTop5(LocalDate date) {
        List<SearchKeyword> top5 = searchKeywordRepository.findTop5BySearchDate(date);

        if (top5.isEmpty()) {
            log.info("[스냅샷] {} 오늘 데이터 없음", date);
            return;
        }

        for (int i = 0; i < top5.size(); i++) {
            SearchKeyword sk = top5.get(i);
            popularKeywordRepository.save(
                    PopularKeyword.builder()
                            .keyword(sk.getKeyword())
                            .rank(i + 1)
                            .searchCount(sk.getSearchCount())
                            .snapshotDate(date)
                            .isFallback(false)
                            .build()
            );
        }

        log.info("[스냅샷] {} Top5 저장 완료 - {}건", date, top5.size());
    }

    /**
     * Redis 오늘 데이터 초기화
     *
     * ZSet(검색어 순위) + Set(유저 기록) 두 개 삭제
     * 날짜별로 키를 만들어서 오늘 것만 삭제
     */
    @Override
    public void clearTodayRedisData(LocalDate date) {
        // ZSet 삭제: "search:rank:<오늘 날짜>"
        redisTemplate.delete(RedisKeyUtils.rankKey(date));

        // Set 삭제: "search:user:<오늘 날짜>"
        redisTemplate.delete(RedisKeyUtils.userLogKey(date));

        log.info("[Redis 초기화] {} 데이터 삭제 완료", date);
    }

    /**
     * Fallback Top5 생성
     *
     * 자정 직후 Redis가 비어있어서 임시 Top5를 생성
     * 최근 7일 데이터에서 다음 순위 5개 뽑아서 isFallback = true 저장
     */
    @Override
    @Transactional
    public void saveFallbackTop5(LocalDate today) {
        LocalDate yesterday = today.minusDays(1);

        // 어제 Top5 키워드 목록 추출
        List<String> yesterdayTop5 = popularKeywordRepository
                .findBySnapshotDateAndIsFallbackFalse(yesterday)
                .stream()
                .map(PopularKeyword::getKeyword)
                .toList();

        if (yesterdayTop5.isEmpty()) {
            log.info("[Fallback] 어제 Top5 없음");
            return;
        }

        // 어제 Top5 제외하고 최근 7일 내 상위 5개 조회
        LocalDate weekAgo = today.minusDays(7);
        List<SearchKeyword> fallbackList = searchKeywordRepository.findTop5ExcludingKeywords(weekAgo, yesterdayTop5);

        if (fallbackList.isEmpty()) {
            log.info("[Fallback] 대체 키워드 없음");
        }

        for (int i = 0; i < fallbackList.size(); i++) {
            SearchKeyword sk = fallbackList.get(i);
            popularKeywordRepository.save(
                    PopularKeyword.builder()
                            .keyword(sk.getKeyword())
                            .rank(i + 1)
                            .searchCount(sk.getSearchCount())
                            .snapshotDate(today)
                            .isFallback(true)
                            .build()
            );
        }

        log.info("[Fallback] {} 임시 Top5 저장 완료", today);
    }
}
