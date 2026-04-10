package jpa.basic.alldayprojectcommerce.domain.user.repository;

import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsUserByEmail(String email);
}
