package jpa.basic.alldayprojectcommerce.domain.payment.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.common.util.IdFactory;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.service.OrderQueryService;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.request.CreatePaymentRequest;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.CreatePaymentResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.entity.Payment;
import jpa.basic.alldayprojectcommerce.domain.payment.entity.PaymentStatus;
import jpa.basic.alldayprojectcommerce.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentCommandServiceImpl implements PaymentCommandService{
    private final PaymentRepository paymentRepository;
    private final OrderQueryService orderQueryService;

    @Override   // 결제 생성 메서드
    public CreatePaymentResponse createPayment(String orderUid, CreatePaymentRequest request, LoginUserInfo loginUser) {

        // 주문 정보 조회(비관적 락 사용)
        Order order = orderQueryService.getOrderByOrderUidForUpdate(orderUid);

        // orderUid를 생성한 주문자와 결제 생성한 로그인 유저가 일치하는지 검증
        Long orderUserId = order.getUserId();
        Long loginUserId = loginUser.id();
        if(!orderUserId.equals(loginUserId)){
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN_ACCESS);
        }

        // 결제 금액 받아오기
        Long amount = request.amount();
        Long deliveryFee = request.deliveryFee();

        // 주문 상태가 PENDING인지 검증
        if(order.getStatus()!= OrderStatus.PENDING){
            throw new CustomException(ErrorCode.ORDER_STATUS_NOT_PENDING);
        }
        // 동일 주문에 대하여 중복 결제 생성은 허용하지만, SUCCESS 상태인 결제가 있으면 결제 생성 막기
        boolean successPaymentExists = paymentRepository.existsByOrderIdAndStatus(order.getId(), PaymentStatus.SUCCESS);
        if (successPaymentExists) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_SUCCESS);
        }

        // 주문금액 검증
        if (amount == null || amount < 0) {  // 정책으로 결제금액 0원 가능
            throw new CustomException(ErrorCode.PAYMENT_INVALID_AMOUNT);
        }
        // 배송비 검증
        if (deliveryFee == null || deliveryFee < 0) {   // 정책으로 배송비 0원 가능
            throw new CustomException(ErrorCode.PAYMENT_INVALID_AMOUNT);
        }
        // TODO : 주문 금액과 배송비가 모두 0원인 경우가 존재하므로 추후 결제 확정 로직에서 결제 금액 0원이면 포트원 호출하지 않고 검증하도록 구현
        // TODO : 프론트에서도 결제 금액이 0원인 경우는 포트원 결제 하지 않도록 구현 필요

        if(!amount.equals(order.getTotalAmount())){
            throw new CustomException(ErrorCode.PAYMENT_INVALID_AMOUNT);
        }
        Payment payment = Payment.builder()
                .paymentUid(createPaymentUid())
                .orderId(order.getId())
                .amount(amount)
                .deliveryFee(deliveryFee)
                .finalAmount(amount + deliveryFee)
                .status(PaymentStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();


        paymentRepository.save(payment);
        return CreatePaymentResponse.from(payment);
    }

    private String createPaymentUid() {
        return IdFactory.generateWithDate("PAY", 8);
    }
}
