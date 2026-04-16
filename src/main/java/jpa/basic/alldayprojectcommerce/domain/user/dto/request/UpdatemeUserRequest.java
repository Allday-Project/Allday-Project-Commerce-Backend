package jpa.basic.alldayprojectcommerce.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatemeUserRequest(

        @Size(max = 20, message = "이름은 최대 20자 입니다.")
        String name,

        @Pattern(
                regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
        )
        String phone,

        String address) {}
