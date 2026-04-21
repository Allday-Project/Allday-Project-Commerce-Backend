package jpa.basic.alldayprojectcommerce.domain.payment.repository;

import jpa.basic.alldayprojectcommerce.domain.payment.entity.Payment;
import jpa.basic.alldayprojectcommerce.domain.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsByOrderIdAndStatus(Long id, PaymentStatus paymentStatus);

    Optional<Payment> findByPaymentUid(String paymentUid);

    boolean existsByOrderIdAndStatusAndIdNot(
            Long orderId,
            PaymentStatus status,
            Long id
    );
}
