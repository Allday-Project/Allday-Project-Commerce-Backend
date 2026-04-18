package jpa.basic.alldayprojectcommerce.domain.cartProduct.service;

import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.request.CreateCartProductRequest;

public interface CartProductCommandService {

    void createCartProduct(Long userId, CreateCartProductRequest request);
}
