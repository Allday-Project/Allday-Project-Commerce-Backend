package jpa.basic.alldayprojectcommerce.domain.keyword.service;

import jpa.basic.alldayprojectcommerce.domain.keyword.dto.response.Top5KeywordResponse;

import java.util.List;

public interface KeywordQueryService {

    // v1 - Redis 실시간 조회
    List<Top5KeywordResponse> getTop5();

    // v2 - Caffeine 인메모리 캐시 적용
    List<Top5KeywordResponse> getTop5Cached();
}
