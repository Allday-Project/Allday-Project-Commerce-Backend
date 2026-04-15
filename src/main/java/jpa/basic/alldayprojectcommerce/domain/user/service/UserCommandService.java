package jpa.basic.alldayprojectcommerce.domain.user.service;

import jpa.basic.alldayprojectcommerce.domain.user.dto.request.UpdatemeUserRequest;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;

public interface UserCommandService {

    User create(String email, String encodedPassword);

    // 내 정보 수정
    void updateProfile(Long userId, UpdatemeUserRequest request);
}
