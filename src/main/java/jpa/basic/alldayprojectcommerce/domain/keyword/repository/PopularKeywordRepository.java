package jpa.basic.alldayprojectcommerce.domain.keyword.repository;

import jpa.basic.alldayprojectcommerce.domain.keyword.entity.PopularKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PopularKeywordRepository extends JpaRepository<PopularKeyword, Long> {


    List<PopularKeyword> findBySnapshotDateOrderByRankAsc(LocalDate snapshotDate);
}
