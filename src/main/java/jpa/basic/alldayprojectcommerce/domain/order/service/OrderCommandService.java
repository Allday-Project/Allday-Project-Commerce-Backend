package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfoDto;
import jpa.basic.alldayprojectcommerce.domain.order.dto.request.CreateOrderRequest;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.CreateOrderResponse;

public interface OrderCommandService {

    // 주문서 생성
    CreateOrderResponse createOrder(LoginUserInfoDto loginUserInfoDto, CreateOrderRequest request);

    // 구매 확정    - PAID 또는 DELIVERY_COMPLETED 상태에서만 가능
    void confirmOrder(LoginUserInfoDto loginUserInfoDto, String orderUid);

    // 주문 취소    - PAID 상태에서만 가능
    void cancelOrder(LoginUserInfoDto loginUserInfoDto, String orderUid);

    /**
     * 결제 성공 시 OrderUser(스냅샷) 저장
     * Payment 도메인의 서비스에서 결제 완료 후 호출 용도
     */
    void saveOrderUser(Long orderId, String name, String phone, String address);

    /**
     * 결제 완료 시 Order 상태를 PAID로 변경
     * Payment 도메인의 서비스에서 호출
     */
    void markOrderPaid(String orderUid);
}
