package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.domain.order.dto.response.OrderDetailResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.OrderSummaryResponse;

public interface OrderQueryService {

    OrderSummaryResponse getAll(Long userId);

    OrderDetailResponse get(String orderRef);
}