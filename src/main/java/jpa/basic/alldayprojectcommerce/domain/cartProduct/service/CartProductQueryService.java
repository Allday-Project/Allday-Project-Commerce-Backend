package jpa.basic.alldayprojectcommerce.domain.cartProduct.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.domain.cartProduct.dto.response.GetAllCartProductResponse;

public interface CartProductQueryService {

    CursorResponse<GetAllCartProductResponse> getAllCartProduct(Long loginId, Long cursorId, int size);
}
