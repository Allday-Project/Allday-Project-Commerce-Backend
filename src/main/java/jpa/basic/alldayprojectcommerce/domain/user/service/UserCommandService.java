package jpa.basic.alldayprojectcommerce.domain.user.service;

import jpa.basic.alldayprojectcommerce.domain.user.dto.request.UpdatemeUserRequest;

public interface UserCommandService {

    // 회원가입에서 사용하는 메서드입니다.
    void create(String email, String encodedPassword);

    // 내 정보 수정
    void updateProfile(Long userId, UpdatemeUserRequest request);
}
