package jpa.basic.alldayprojectcommerce.domain.keyword.service;

import java.time.LocalDate;

public interface KeywordCommandService {

    // 회원용   - userId 기반 중복 방지
    void recordSearch(Long userId, String query);

    // 비회원용 - IP 기반 중복 방지
    void recordSearchByIp(String ip, String query);

    /**
     * Redis -> DB Write-back
     * 1시간마다 스케쥴러가 호출
     */
    void writeBack();

    // Top5 스냅샷 저장
    void snapshotTop5(LocalDate date);

    // Redis 초기화
    void clearTodayRedisData(LocalDate date);

    // Fallback Top5 생성
    void saveFallbackTop5(LocalDate today);
}
