package jpa.basic.alldayprojectcommerce.domain.payment.service;

import groovy.util.logging.Slf4j;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfoDto;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.service.OrderQueryService;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.request.CreatePaymentRequest;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.CreatePaymentResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.entity.Payment;
import jpa.basic.alldayprojectcommerce.domain.payment.entity.PaymentStatus;
import jpa.basic.alldayprojectcommerce.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentCommandServiceImpl implements PaymentCommandService{
    private final PaymentRepository paymentRepository;
    private final OrderQueryService orderQueryService;

    @Override   // 결제 생성 메서드
    public CreatePaymentResponse createPayment(String orderUid, CreatePaymentRequest request, LoginUserInfoDto loginUser) {

        // orderUid 존재 여부 검증
        if (!StringUtils.hasText(orderUid)) {
            throw new CustomException(ErrorCode.ORDER_INVALID_UID);
        }

        // 주문 정보 조회
        Order order = orderQueryService.getOrderByOrderUid(orderUid);

        // orderUid를 생성한 주문자와 결제 생성한 로그인 유저가 일치하는지 검증
        Long orderUserId = order.getUser().getId();
        Long loginUserId = loginUser.id();
        if(!orderUserId.equals(loginUserId)){
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // 결제 금액 받아오기
        Long amount = request.amount();
        Long deliveryFee = request.deliveryFee();

        // 주문 상태 검증
        if(order.getStatus()!= OrderStatus.PENDING){
            throw new CustomException(ErrorCode.ORDER_STATUS_NOT_PENDING);
        }
        // 동일 주문에 대하여 중복 결제 생성은 허용하지만, SUCCESS 상태인 결제가 있으면 결제 생성 막기
        boolean existence = paymentRepository.existsByOrderIdAndStatus(order.getId(), PaymentStatus.SUCCESS);
        if (existence) {
            throw new CustomException(ErrorCode.PAYMENT_ALREADY_SUCCESS);
        }

        // 주문금액 검증
        if (amount == null) {
            throw new CustomException(ErrorCode.PAYMENT_INVALID_AMOUNT);
        }
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
        // TODO : 성현님 코드 머지 되면 nanoid 생성하는 클래스 사용해서 만들기
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
