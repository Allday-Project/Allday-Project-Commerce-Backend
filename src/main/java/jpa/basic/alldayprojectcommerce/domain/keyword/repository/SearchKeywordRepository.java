package jpa.basic.alldayprojectcommerce.domain.keyword.repository;

import jpa.basic.alldayprojectcommerce.domain.keyword.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    Optional<SearchKeyword> findByKeywordAndSearchDate(String keyword, LocalDate searchDate);

    /**
     * 오늘 날짜 Top5 추출 - 자정 스케쥴러
     *
     * SELECT *
     * FROM SearchKeyword sk
     * WHERE sk.searchDate = :date
     * ORDER BY sk.searchCount DESC
     * LIMIT 5;
     */
    @Query("""
            SELECT s
            FROM SearchKeyword s
            WHERE s.searchDate = :date
            ORDER BY s.searchCount DESC
            LIMIT 5
            """)
    List<SearchKeyword> findTop5BySearchDate(@Param("date") LocalDate date);

    /**
     * 특정 키워드를 제외하고 최근 7일 내 상위 5개 - Fallback
     *
     * SELECT *
     * FROM SearchKeyword sk
     * WHERE sk.searchDate >= :fromDate
     * AND sk.keyword NOT IN (:excludeKeywords)
     * ORDER BY sk.searchCount DESC
     * LIMIT 5
     */
    @Query("""
            SELECT s
            FROM SearchKeyword s
            WHERE s.searchDate >= :fromDate
            AND s.keyword NOT IN :excludeKeywords
            ORDER BY s.searchCount DESC
            LIMIT 5
            """)
    List<SearchKeyword> findTop5ExcludingKeywords(@Param("fromDate") LocalDate fromDate,
                                                  @Param("excludeKeywords") List<String> excludeKeywords);

    List<SearchKeyword> findBySearchDate(LocalDate today);
}
