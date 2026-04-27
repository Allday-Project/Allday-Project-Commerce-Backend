package jpa.basic.alldayprojectcommerce.common.config;

import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import jpa.basic.alldayprojectcommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Profile("local")
@Component
@RequiredArgsConstructor
public class DummyDataUser implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {

        // 이미 유저 데이터가 있으면 중복 생성하지 않음
        if (userRepository.count() > 0) {
            return;
        }

        // 10,000명 더미 유저 생성
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= 10000; i++) {
            // 이메일과 비밀번호를 먼저 세팅하여 User 엔티티 생성
            User user = User.createUser(
                    "user" + i + "@test.com",
                    "encoded-password-" + i
            );

            // 주문 가능 조건에 필요한 프로필 정보 세팅
            user.updateProfile(
                    "유저" + i,
                    "010-1234-" + String.format("%04d", i % 10000),
                    "서울시 강남구 테스트로 " + i
            );

            users.add(user);

            // 1000명 단위로 배치 저장하여 메모리 사용량 절약
            if (i % 1000 == 0) {
                userRepository.saveAll(users);
                users.clear();
            }
        }

        // 혹시 남은 데이터가 있으면 마지막 저장
        if (!users.isEmpty()) {
            userRepository.saveAll(users);
        }
    }
}