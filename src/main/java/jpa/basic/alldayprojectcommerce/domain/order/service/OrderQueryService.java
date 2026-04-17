package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetAllOrdersResponse;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;

public interface OrderQueryService {

    CursorResponse<GetAllOrdersResponse> getAllOrders(LoginUserInfo loginUserInfo, Long cursorId, int size);

    Order getOrderByOrderUid(String orderUid);
    Order getOrderByOrderUidForUpdate(String orderUid);

}