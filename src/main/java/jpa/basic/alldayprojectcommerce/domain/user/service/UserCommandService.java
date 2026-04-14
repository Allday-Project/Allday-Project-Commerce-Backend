package jpa.basic.alldayprojectcommerce.domain.user.service;

public interface UserCommandService {

    // 회원가입에서 사용하는 메서드입니다.
    void create(String email, String encodedPassword);
}
