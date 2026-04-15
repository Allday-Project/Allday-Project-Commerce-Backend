package jpa.basic.alldayprojectcommerce.domain.user.repository;

import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsUserByEmail(String email);

    Optional<User> findByEmail(String email);

    List<User> id(Long id);
}
