package jpa.basic.alldayprojectcommerce.domain.product.repository;

import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustomImpl {
}
