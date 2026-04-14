package jpa.basic.alldayprojectcommerce.domain.user.dto.response;

import jpa.basic.alldayprojectcommerce.domain.user.entity.User;

public record UpdatemeUserResponse(
        Long id,
        String email,
        String name,
        String phone,
        String address) {

    public static UpdatemeUserResponse from(User user) {
        return new UpdatemeUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhone(),
                user.getAddress()
        );
    }
}
