package jpa.basic.alldayprojectcommerce.domain.keyword.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
        @NotBlank(message = "검색어를 입력해주세요.")
        String query
) {
}
