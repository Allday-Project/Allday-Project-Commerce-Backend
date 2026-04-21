package jpa.basic.alldayprojectcommerce.domain.keyword.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "search_keywords")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchKeyword extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String keyword;                 // 사용자가 검색한 단어

    @Min(value = 0)
    @Column(nullable = false)
    private Long searchCount;              // 키워드가 검색된 총 횟수 (특정 날짜 기준)

    @Column(nullable = false)
    private LocalDate searchDate;      // 어느 날짜의 기록인지

    @Builder
    public SearchKeyword(String keyword, Long searchCount, LocalDate searchDate) {
        this.keyword = keyword;
        this.searchCount = searchCount;
        this.searchDate = searchDate;
    }

    // Write-back 시 Redis 카운트를 DB에 덮어쓰기
    public void setCount(Long count) {
        this.searchCount = count;
    }
}
