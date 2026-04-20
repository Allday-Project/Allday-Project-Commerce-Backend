package jpa.basic.alldayprojectcommerce.domain.keyword.service;

public interface KeywordCommandService {

    /**
     * 검색어 기록
     * 고객이 검색창에 입력한 문장을 받아서 Redis ZSet에 카운트 + 1
     */
    void createRecordSearch(Long loginId, String query);

    /**
     * Redis -> DB Write-back
     * 1시간마다 스케쥴러가 호출
     */
    void writeBack();
}
