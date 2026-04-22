package jpa.basic.alldayprojectcommerce.domain.keyword.service;

import jpa.basic.alldayprojectcommerce.common.util.RedisKeyUtils;
import jpa.basic.alldayprojectcommerce.domain.keyword.dto.response.Top5KeywordResponse;
import jpa.basic.alldayprojectcommerce.domain.keyword.entity.PopularKeyword;
import jpa.basic.alldayprojectcommerce.domain.keyword.repository.PopularKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordQueryServiceImpl implements KeywordQueryService {

    private final RedisTemplate<String, String>  redisTemplate;
    private final PopularKeywordRepository popularKeywordRepository;

    /**
     * 인기 검색어 Top5 조회
     * 1. Redis에서 실시간 Top5 조회 시도
     * 2. Redis가 비어있거나 장애 시 DB fallback
     */
    @Override
    @Transactional(readOnly = true)
    public List<Top5KeywordResponse> getTop5() {

        // Redis에서 Top5 조회 시도
        List<Top5KeywordResponse> redisResult = getTop5FromRedis();

        if (!redisResult.isEmpty()) {
            log.debug("[인기 검색어 Top5 조회] Redis에서 반환");
            return redisResult;
        }

        // Redis가 비어있거나 장애 시 DB fallback
        log.info("[인기 검색어 Top5 조회] Redis에 없음 -> DB fallback 전환");
        return getTop5FromDb();
    }

    /**
     * Redis ZSet에서 Top5 조회
     * TypedTuple {value: "Allday", score: 10000.0} 형태
     */
    private List<Top5KeywordResponse> getTop5FromRedis() {
        try {
            // score 기준 내림차순 (5개 조회)
            Set<ZSetOperations.TypedTuple<String>> result =
                    redisTemplate.opsForZSet()
                            .reverseRangeWithScores(RedisKeyUtils.todayRankKey(), 0, 4);

            // Redis에 데이터 없음 (자정 초기화 등)
            if (result == null || result.isEmpty()) {
                return Collections.emptyList();
            }

            // TypedTuple -> Top5KeywordResponse 변환
            List<Top5KeywordResponse> responses = new ArrayList<>();
            int rank = 1;

            for (ZSetOperations.TypedTuple<String> tuple : result) {
                responses.add(new Top5KeywordResponse(rank++, tuple.getValue()));
            }

            return responses;

        } catch (Exception e) {
            log.warn("[인기 검색어 Top5 조회] Redis 조회 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * v2 - Caffeine 캐시 적용 Top5 조회
     *
     * value    : 캐시 저장소 이름
     * key      : 캐시 데이터 식별자
     */
    @Override
    @Cacheable(value = "top5Keywords", key = "'top5'")
    public List<Top5KeywordResponse> getTop5Cached() {
        log.debug("[Top5 v2] 캐시 미스 → Redis/DB 조회");
        // 캐시가 없을 때만 실행
        return getTop5();
    }

    /**
     * DB(PopularKeyword)에서 Top5 조회
     * 오늘 날짜 스냅샷이 없으면 어제 날짜로 재시도
     * 어제 데이터도 없으면 빈 리스트 반환
     */
    private List<Top5KeywordResponse> getTop5FromDb() {
        // 오늘 스냅샷 조회
        List<PopularKeyword> todayList =
                popularKeywordRepository.findBySnapshotDateOrderByRankAsc(LocalDate.now());

        if (!todayList.isEmpty()) {
            return todayList.stream()
                    .map(Top5KeywordResponse::from)
                    .toList();
        }

        // 어제 날짜 시도
        log.info("[인기 검색어 Top5 조회] 오늘 데이터 없음 -> 어제 데이터로 재시도");
        List<PopularKeyword> yesterdayList =
                popularKeywordRepository.findBySnapshotDateOrderByRankAsc(LocalDate.now().minusDays(1));

        return yesterdayList.stream()
                .map(Top5KeywordResponse::from)
                .toList();
    }
}
