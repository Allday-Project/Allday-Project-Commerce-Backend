package jpa.basic.alldayprojectcommerce.domain.keyword.repository;

import jpa.basic.alldayprojectcommerce.domain.keyword.entity.PopularKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface PopularKeywordRepository extends JpaRepository<PopularKeyword, Long> {

    List<PopularKeyword> findBySnapshotDateOrderByRankAsc(LocalDate snapshotDate);

    // Fallback 생성 시 어제 Top5 키워드 목록 조회
    List<PopularKeyword> findBySnapshotDateAndIsFallbackFalse(LocalDate snapshotDate);

    // 오늘 Fallback 삭제
    @Modifying
    @Query("""
           DELETE FROM PopularKeyword p
           WHERE p.snapshotDate = :date
           AND p.isFallback = true
           """)
    void deleteBySnapshotDateAndIsFallbackTrue(@Param("date") LocalDate snapshotDate);
}
