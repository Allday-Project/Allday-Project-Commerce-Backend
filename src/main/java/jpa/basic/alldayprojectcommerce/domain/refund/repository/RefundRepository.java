package jpa.basic.alldayprojectcommerce.domain.refund.repository;

import jpa.basic.alldayprojectcommerce.domain.refund.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund,Long> {
}
