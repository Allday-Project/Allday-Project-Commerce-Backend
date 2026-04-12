package jpa.basic.alldayprojectcommerce.common.security.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @Email @NotBlank
        String email,
        @NotBlank @Size(min = 8, max = 20)
        String password
) {
}
