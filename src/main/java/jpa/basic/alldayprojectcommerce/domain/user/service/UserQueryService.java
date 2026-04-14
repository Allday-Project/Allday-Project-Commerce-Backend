package jpa.basic.alldayprojectcommerce.domain.user.service;

import jpa.basic.alldayprojectcommerce.domain.user.entity.User;

import java.util.Optional;

public interface UserQueryService {

    Optional<User> getByEmail(String email);

    User getById(Long userId);
}
