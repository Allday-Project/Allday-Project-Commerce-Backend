package jpa.basic.alldayprojectcommerce.domain.product.repository;

import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ProductRepositoryCustom {

    Page<Product> findAllProducts(Pageable pageable);


}
