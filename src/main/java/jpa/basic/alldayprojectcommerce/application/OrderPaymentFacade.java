package jpa.basic.alldayprojectcommerce.application;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfoDto;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.service.OrderQueryService;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.ConfirmPaymentResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.entity.Payment;
import jpa.basic.alldayprojectcommerce.domain.payment.entity.PaymentStatus;
import jpa.basic.alldayprojectcommerce.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OrderPaymentFacade {

    private final OrderQueryService orderQueryService;
    private final PaymentRepository paymentRepository;

    @Transactional
    public ConfirmPaymentResponse confirmOrderPayment(
            String orderUid,
            String paymentUid,
            LoginUserInfoDto loginUser
    ) {
        Long loginUserId = loginUser.id();

        // orderUid 존재 여부 검증
        if (!StringUtils.hasText(orderUid)) {
            throw new CustomException(ErrorCode.ORDER_INVALID_UID);
        }

        // paymentUid 존재 여부 검증
        if (!StringUtils.hasText(paymentUid)) {
            throw new CustomException(ErrorCode.PAYMENT_INVALID_UID);
        }

        // Order 객체 조회
        Order order = orderQueryService.getOrderByOrderUidForUpdate(orderUid);

        // 로그인 유저가 생성한 주문이 맞는지 검증
        Long orderUserId = order.getUser().getId();
        if (!loginUserId.equals(orderUserId)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // Payment 객체 조회. 없으면 생성되지 않은 결제 요청
        Payment payment = paymentRepository.findByPaymentUid(paymentUid).orElseThrow(
                () -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND)
        );

        // 검증한 주문 건에 대해 생성한 결제 건이 맞는지 확인
        if (!order.getId().equals(payment.getOrderId())) {
            throw new CustomException(ErrorCode.PAYMENT_ORDER_NOT_MATCHES);
        }

        // Payment 상태 검증. 결제 대기 상태가 아니라면 이미 처리된 결제 요청이므로 확정 요청이 올 수 없음
        // 이미 존재하던 결제 상태를 반환
        if (PaymentStatus.PENDING != payment.getStatus()) {
            return ConfirmPaymentResponse.of(orderUid, payment.getStatus());
        }

        // Order 상태 검증
        if (OrderStatus.PENDING != order.getStatus()) {
            if (OrderStatus.PAID == order.getStatus()) {
                return ConfirmPaymentResponse.of(orderUid, PaymentStatus.SUCCESS);
            }
            throw new CustomException(ErrorCode.ORDER_STATUS_NOT_PENDING);
        }

        // 주문 건에 대하여 이미 성공한 결제 건이 있는지 검증
        boolean existsAnotherSuccess =
                paymentRepository.existsByOrderIdAndStatusAndIdNot(order.getId(), PaymentStatus.SUCCESS, payment.getId());

        if (existsAnotherSuccess) {
            return ConfirmPaymentResponse.of(orderUid, PaymentStatus.SUCCESS);
        }

        // TODO : 포트원 검증 로직

        // 우선 결제 확정 요청이 들어오면 검증을 통과하면 성공한 것으로 응답 추후 포트원 연동 검증 구현 예정
        payment.markSuccess();
        order.markPaid();
        // TODO : 재고차감 로직 추가 필요

        return ConfirmPaymentResponse.of(orderUid, PaymentStatus.SUCCESS);
    }

}