package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetAllOrdersResponse;

public interface OrderQueryService {

    CursorResponse<GetAllOrdersResponse> getAllOrders(LoginUserInfo loginUserInfo, Long cursorId, int size);
}