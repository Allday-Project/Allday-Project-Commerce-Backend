package jpa.basic.alldayprojectcommerce.domain.product.service;

import jpa.basic.alldayprojectcommerce.domain.product.dto.request.ProductUpdateRequest;
import jpa.basic.alldayprojectcommerce.domain.product.dto.response.ProductUpdateResponse;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;

public interface ProductCommandService {

    void decreaseStock(Long productId, int quantity, Long orderId);
    void increaseStock(Long productId, int quantity, Long orderId);
    void saveStockHistory(Product product, int quantity, Long orderId);
    void checkStock(Long productId, int quantity);
    ProductUpdateResponse updateProduct(Long productId, ProductUpdateRequest request);



}
