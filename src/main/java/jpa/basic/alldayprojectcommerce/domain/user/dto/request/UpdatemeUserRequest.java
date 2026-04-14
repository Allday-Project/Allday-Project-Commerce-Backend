package jpa.basic.alldayprojectcommerce.domain.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdatemeUserRequest(

        @Size(max = 20, message = "이름은 최대 20자 입니다.")
        String name,

        @Size(max = 100, message = "전화번호는 최대 100자 입니다.")
        String phone,

        String address,

        //@NotBlank..
        @Size(min = 8, max = 20, message = "새 비밀번호는 최소 8자 ~ 20자입니다.")
        String password) {}
