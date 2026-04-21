package jpa.basic.alldayprojectcommerce.domain.keyword.entity;

import jakarta.persistence.*;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "popular_keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularKeyword extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String keyword;         // 인기검색어 단어

    @Column(name = "keyword_rank", nullable = false)
    private Integer rank;               // 순위

    @Column(nullable = false)
    private Long searchCount;       // 특정 순위일 때 검색 횟수

    @Column(nullable = false)
    private LocalDate snapshotDate;      // 어느 날짜의 Top5 인지

    @Column(nullable = false)
    private boolean isFallback;     // Top5 -> false, 대체 키워드 -> true

    public PopularKeyword(String keyword, Integer rank, Long searchCount, LocalDate snapshotDate, boolean isFallback) {
        this.keyword = keyword;
        this.rank = rank;
        this.searchCount = searchCount;
        this.snapshotDate = snapshotDate;
        this.isFallback = isFallback;
    }
}
