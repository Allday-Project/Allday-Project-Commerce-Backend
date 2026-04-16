package jpa.basic.alldayprojectcommerce.domain.user.repository;

import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 회원가입 시 이메일 중복확인
    boolean existsUserByEmail(String email);

    // 로그인 시 유저DB에 있는 email인지 조회
    Optional<User> findByEmail(String email);

    //?????
    List<User> id(Long id);
}
