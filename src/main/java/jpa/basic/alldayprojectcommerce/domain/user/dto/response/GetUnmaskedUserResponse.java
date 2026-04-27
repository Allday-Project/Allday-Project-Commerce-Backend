package jpa.basic.alldayprojectcommerce.domain.user.dto.response;

import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import lombok.Builder;

@Builder
public record GetUnmaskedUserResponse(
    Long id,
    String email,
    String name,
    String password,
    String phone,
    String address
) {
    public static GetUnmaskedUserResponse from(User user) {
        return new GetUnmaskedUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getPhone(),
                user.getAddress()
        );
    }
}
