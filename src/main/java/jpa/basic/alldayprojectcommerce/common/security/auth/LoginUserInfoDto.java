package jpa.basic.alldayprojectcommerce.common.security.auth;

import lombok.Builder;

@Builder
public record LoginUserInfoDto(
        Long id
) {
}