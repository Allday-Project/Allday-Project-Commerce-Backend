package jpa.basic.alldayprojectcommerce.domain.user.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.user.dto.request.UpdatemeUserRequest;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import jpa.basic.alldayprojectcommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Override
    public void create(String email, String encodedPassword) {
        if (userRepository.existsUserByEmail(email)) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }
        userRepository.save(User.createUser(email, encodedPassword));
    }

    // 내 정보 수정
    @Override
    public void updateProfile(Long userId, UpdatemeUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow( () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이름, 전화번호, 주소 수정
        user.updateProfile(request.name(), request.password(), request.phone(), request.address());

//        // 비밀번호 수정 (임시 비밀번호..)
//        if(request.password() != null && !request.password().isBlank()) {
//            user.updatePassword(passwordEncoder.encode(request.password()));
//        }
        // @Transactional에 의해 dirty-checking으로 자동 저장됨.
    }

    // 내 정보 수정
    @Override
    public void updateProfile(Long userId, UpdatemeUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow( () -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이름, 전화번호, 주소 수정
        user.updateProfile(request.name(), request.password(), request.phone(), request.address());

//        // 비밀번호 수정 (임시 비밀번호..)
//        if(request.password() != null && !request.password().isBlank()) {
//            user.updatePassword(passwordEncoder.encode(request.password()));
//        }
        // @Transactional에 의해 dirty-checking으로 자동 저장됨.
    }
}

