package jpa.basic.alldayprojectcommerce.domain.user.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.user.dto.request.UpdatePasswordRequest;
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
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이름, 전화번호, 주소 수정내역 반영
        user.updateProfile(request.name(), request.phone(), request.address());
    }

    // 비밀번호 변경
    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 1. 현재 비밀번호 검증
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_PASSWORD_NOT_MATCH);
        }

        // 2. 현재 비밀번호랑 동일한지 검증
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_PASSWORD_SAME_AS_CURRENT);
        }

        // 3. 새 비밀번호 인코딩 후 저장
        String encodedNewPassword = passwordEncoder.encode(request.newPassword());
        user.updatePassword(encodedNewPassword);
    }
}

