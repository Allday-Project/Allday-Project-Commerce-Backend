package jpa.basic.alldayprojectcommerce.domain.user.service;

import jpa.basic.alldayprojectcommerce.domain.user.entity.User;

public interface UserCommandService {

    User create(String email, String encodedPassword);
}
