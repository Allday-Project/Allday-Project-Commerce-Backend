package jpa.basic.alldayprojectcommerce.domain.keyword.dto.response;

import jpa.basic.alldayprojectcommerce.domain.keyword.entity.PopularKeyword;

public record Top5KeywordResponse(
        int rank,
        String keyword
) {

    // Redis에서 가져올 때
    public static Top5KeywordResponse of(int rank, String keyword) {
        return new Top5KeywordResponse(rank, keyword);
    }

    // DB(PopularKeyword)에서 가져올 때
    public static Top5KeywordResponse from(PopularKeyword popularKeyword) {
        return new Top5KeywordResponse(
                popularKeyword.getRank(),
                popularKeyword.getKeyword()
        );
    }
}
