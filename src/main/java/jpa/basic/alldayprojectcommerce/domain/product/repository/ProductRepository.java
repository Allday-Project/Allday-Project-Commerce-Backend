package jpa.basic.alldayprojectcommerce.domain.product.repository;

import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE products
        SET stock = stock - :quantity,
            status = CASE
                WHEN stock - :quantity = 0 THEN 'SOLD_OUT'
                ELSE status
            END
        WHERE id = :productId
          AND stock >= :quantity
          AND status = 'ON_SALE'
    """, nativeQuery = true)
    int decreaseStockIfAvailable(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE products
        SET stock = stock + :quantity,
            status = CASE
                WHEN status = 'SOLD_OUT' AND stock + :quantity > 0 THEN 'ON_SALE'
                ELSE status
            END
        WHERE id = :productId
    """, nativeQuery = true)
    int increaseStock(
            @Param("productId") Long productId,
            @Param("quantity") int quantity
    );

    @Query("select p.stock from Product p where p.id = :productId")
    Integer findStockByProductId(@Param("productId") Long productId);
}
