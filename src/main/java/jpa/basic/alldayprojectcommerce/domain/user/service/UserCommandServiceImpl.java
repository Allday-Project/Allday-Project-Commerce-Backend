package jpa.basic.alldayprojectcommerce.domain.user.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import jpa.basic.alldayprojectcommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;

    @Override
    public User create(String email, String encodedPassword) {
        if (userRepository.existsUserByEmail(email)) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }
        return userRepository.save(User.createUser(email, encodedPassword));
    }
}

