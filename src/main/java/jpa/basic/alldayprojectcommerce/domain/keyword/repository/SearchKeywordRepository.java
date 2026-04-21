package jpa.basic.alldayprojectcommerce.domain.keyword.repository;

import jpa.basic.alldayprojectcommerce.domain.keyword.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    Optional<SearchKeyword> findByKeywordAndSearchDate(String keyword, LocalDate searchDate);
}
