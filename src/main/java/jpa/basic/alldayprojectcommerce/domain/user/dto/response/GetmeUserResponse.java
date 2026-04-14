package jpa.basic.alldayprojectcommerce.domain.user.dto.response;

import jpa.basic.alldayprojectcommerce.common.util.MaskingUtils;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import lombok.Builder;

@Builder
public record GetmeUserResponse(
    Long id,
    String email,
    String name,
    String password,
    String phone,
    String address
) {
    public static GetmeUserResponse from(User user) {
        return new GetmeUserResponse(
                user.getId(),
                MaskingUtils.maskEmail(user.getEmail()),
                MaskingUtils.maskName(user.getName()),
                MaskingUtils.maskPassword(),
                MaskingUtils.maskPhone(user.getPhone()),
                MaskingUtils.maskAddress(user.getAddress())
        );
    }
}
