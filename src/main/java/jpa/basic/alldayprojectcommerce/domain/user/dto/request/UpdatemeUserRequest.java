package jpa.basic.alldayprojectcommerce.domain.user.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatemeUserRequest(

        @Size(max = 20, message = "이름은 최대 20자 입니다.")
        String name,

        @Size(max = 100, message = "전화번호는 최대 100자 입니다.")
        @Pattern(
                regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$",
                message = "휴대폰 번호 형식이 올바르지 않습니다. (예: 010-1234-5678)"
        )
        String phone,

        @Size(max = 255)
        String address,

        //@NotBlank(message = "비밀번호는 필수 값입니다.")
        @Size(min = 8, max = 20, message = "새 비밀번호는 최소 8자 ~ 20자입니다.")
        String password) {}
