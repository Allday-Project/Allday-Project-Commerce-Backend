package jpa.basic.alldayprojectcommerce.domain.order.repository;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    @NonNull Optional<Order> findById(@NonNull String id);

    @NonNull Page<Order> findAll(@NonNull Pageable pageable);
}