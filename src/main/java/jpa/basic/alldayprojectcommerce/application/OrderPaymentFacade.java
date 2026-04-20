package jpa.basic.alldayprojectcommerce.application;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.OrderProductInfo;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.service.OrderCommandService;
import jpa.basic.alldayprojectcommerce.domain.order.service.OrderQueryService;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.ConfirmPaymentResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.ConfirmPaymentResult;
import jpa.basic.alldayprojectcommerce.domain.payment.service.PaymentCommandService;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductCommandService;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderPaymentFacade {

    private final OrderQueryService orderQueryService;
    private final PaymentCommandService paymentCommandService;
    private final UserQueryService userQueryService;
    private final OrderCommandService orderCommandService;
    private final ProductCommandService productCommandService;

    @Transactional
    public ConfirmPaymentResponse confirmOrderPayment(
            String orderUid,
            String paymentUid,
            Long loginUserId
    ) {

        validateOrderUid(orderUid);

        // Order 객체 조회
        Order order = orderQueryService.getOrderByOrderUidForUpdate(orderUid);

        // 주문자와 로그인 유저가 일치하는지 검증
        validateOrderOwner(order, loginUserId);

        // 결제 확정 검증 및 결제 상태 변경
        ConfirmPaymentResult result = paymentCommandService.confirmPayment(order, paymentUid);

        // 오직 "이번 호출에서 실제 성공 처리된 경우"에만 후속 처리 수행
        if (result.newlyConfirmed()) {
            User user = userQueryService.getById(loginUserId);

            if (!user.hasRequiredInfo()) {
                throw new CustomException(ErrorCode.USER_ORDERER_INFO_REQUIRED);
            }

            // 1. 재고 차감
            List<OrderProductInfo> orderProducts = orderQueryService.getOrderProducts(order.getId());

            for (OrderProductInfo orderProduct : orderProducts) {
                productCommandService.decreaseStock(orderProduct.productId(), orderProduct.quantity(), order.getId());
            }

            // 2. OrderUser 스냅샷 저장
            orderCommandService.saveOrderUser(order.getId(), user.getName(), user.getPhone(), user.getAddress());

            // 3. 주문 완료 처리
            orderCommandService.markOrderComplete(order);

            // 4. 배송 완료 처리
            orderCommandService.markOrderDelivered(order);
        }
        return ConfirmPaymentResponse.of(orderUid, result.paymentStatus());

    }

    private void validateOrderUid(String orderUid) {
        if (!StringUtils.hasText(orderUid)) {
            throw new CustomException(ErrorCode.ORDER_INVALID_UID);
        }
    }

    private void validateOrderOwner(Order order, Long loginUserId) {
        if (!loginUserId.equals(order.getUserId())) {
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN_ACCESS);
        }
    }

}