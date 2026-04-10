package jpa.basic.alldayprojectcommerce.common.security.auth.dto;

public record CreateUserRequest(
        String name,
        String email,
        String password
) {
}
