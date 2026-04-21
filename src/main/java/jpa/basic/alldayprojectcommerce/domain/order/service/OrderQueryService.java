package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetAllOrdersResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetOneOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetOrderDetailsResponse;

import jpa.basic.alldayprojectcommerce.domain.order.dto.response.OrderProductInfo;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;

import java.util.List;

public interface OrderQueryService {

    CursorResponse<GetAllOrdersResponse> getAllOrder(Long loginId, Long cursorId, int size);

    GetOneOrderResponse getOneOrder(Long loginId, String orderUid);

    GetOrderDetailsResponse getOneOrderDetail(Long loginId, String orderUid);

    Order getOrderByOrderUidForUpdate(String orderUid);

    List<OrderProductInfo> getOrderProducts(Long orderId);


}