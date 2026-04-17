package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetAllOrdersResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetOneOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetOrderDetailsResponse;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;

public interface OrderQueryService {

    CursorResponse<GetAllOrdersResponse> getAllOrders(Long loginId, Long cursorId, int size);

    GetOneOrderResponse getOneOrder(Long loginId, String orderUid);

    GetOrderDetailsResponse getOrderDetails(Long loginId, String orderUid);

    Order getOrderByOrderUid(String orderUid);
    Order getOrderByOrderUidForUpdate(String orderUid);

}