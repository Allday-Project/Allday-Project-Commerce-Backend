package jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.response;

import jpa.basic.alldayprojectcommerce.domain.cartProduct.entity.CartProduct;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;

public record GetAllCartProductResponse(
   Long cartProductId,
   Long userId,
   Long productId,
   String productName,
   Long price,
   int quantity,
   Long subtotal // price * quantity
) {
    public static GetAllCartProductResponse from(CartProduct cartProduct, Product product) {
        return new GetAllCartProductResponse(
                cartProduct.getId(),
                cartProduct.getUserId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                cartProduct.getQuantity(),
                product.getPrice() * cartProduct.getQuantity()
        );
    }
}
