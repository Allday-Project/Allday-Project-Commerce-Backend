package jpa.basic.alldayprojectcommerce.domain.payment.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.util.IdFactory;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.service.OrderQueryService;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.request.CreatePaymentRequest;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.ConfirmPaymentResult;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.CreatePaymentResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.entity.Payment;
import jpa.basic.alldayprojectcommerce.domain.payment.entity.PaymentStatus;
import jpa.basic.alldayprojectcommerce.domain.payment.repository.PaymentRepository;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserQueryService;
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
    private final UserQueryService userQueryService;

    @Override   // 결제 생성 메서드
    public CreatePaymentResponse createPayment(String orderUid, CreatePaymentRequest request, Long loginUserId) {

        // 주문 정보 조회(비관적 락 사용)
        Order order = orderQueryService.getOrderByOrderUidForUpdate(orderUid);

        // orderUid를 생성한 주문자와 결제 생성한 로그인 유저가 일치하는지 검증
        Long orderUserId = order.getUserId();

        if(!orderUserId.equals(loginUserId)){
            throw new CustomException(ErrorCode.AUTH_FORBIDDEN_ACCESS);
        }

        // 주문자 정보 존재 여부 검증

        boolean hasRequiredOrdererInfo = userQueryService.hasRequiredOrdererInfo(orderUserId);

        if (!hasRequiredOrdererInfo) {
            throw new CustomException(ErrorCode.USER_ORDERER_INFO_REQUIRED);
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

    @Override
    public ConfirmPaymentResult confirmPayment(Order order, String paymentUid) {

        // paymentUid 존재 여부 검증
        if (!StringUtils.hasText(paymentUid)) {
            throw new CustomException(ErrorCode.PAYMENT_INVALID_UID);
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
            return ConfirmPaymentResult.alreadyProcessed(payment.getStatus());
        }

        // Order 상태 검증
        if (OrderStatus.PENDING != order.getStatus()) {
            if (OrderStatus.COMPLETED == order.getStatus()) {
                return ConfirmPaymentResult.alreadyProcessed(PaymentStatus.SUCCESS);
            }
            throw new CustomException(ErrorCode.ORDER_STATUS_NOT_PENDING);
        }

        // 주문 건에 대하여 이미 성공한 결제 건이 있는지 검증.
        boolean existsAnotherSuccess =
                paymentRepository.existsByOrderIdAndStatusAndIdNot(order.getId(), PaymentStatus.SUCCESS, payment.getId());

        if (existsAnotherSuccess) {
            return ConfirmPaymentResult.alreadyProcessed(PaymentStatus.SUCCESS);
        }

        // TODO : 포트원 검증 로직

        // 우선 결제 확정 요청이 들어오면 검증을 통과하면 성공한 것으로 응답 추후 포트원 연동 검증 구현 예정
        payment.markSuccess();

        return ConfirmPaymentResult.newlyConfirmed(PaymentStatus.SUCCESS);
    }

    private String createPaymentUid() {
        return IdFactory.generateWithDate("PAY", 8);
    }
}
