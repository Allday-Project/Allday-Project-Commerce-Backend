package jpa.basic.alldayprojectcommerce.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(

    @NotBlank(message = "현재 비밀번호를 입력하세요")
    String currentPassword,

    @NotBlank
    @Size(min = 8, max = 20, message = "새 비밀번호는 최소 8자 ~ 20자입니다.")
    String newPassword
) {}
