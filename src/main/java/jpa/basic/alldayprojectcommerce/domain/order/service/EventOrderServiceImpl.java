package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.util.IdFactory;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderProduct;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderProductRepository;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductCommandService;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryService;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EventOrderServiceImpl implements EventOrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductQueryService productQueryService;
    private final UserQueryService userQueryService;
    private final ProductCommandService productCommandService;
    private final OrderCommandService orderCommandService;

    /*
    동시성 확인을 위한 이벤트 티켓 무료 나눔 메서드
    유저 검증
    재고 검증 후 차감
    주문 완료 상태로 저장(COMPLETED), 결제가 없으니 PENDING 없음
    주문 상품 스냅샷 저장
    주문 유저 스냅샷 저장
     */
    @Override
    public EventOrderResponse createEventOrder(Long productId, Long userId) {

        // 이벤트 아이템은 항상 유저 한명당 1개만 가능
        int quantity = 1;

        // 유저 검증
        // 유저 정보를 사전에 미리 등록해놨어어야 주문 가능하도록 설계
        User user = userQueryService.getById(userId);
        if (!user.hasRequiredInfo()) {
            throw new CustomException(ErrorCode.USER_ORDERER_INFO_REQUIRED);
        }

        // 상품 존재 검증
        Product product = productQueryService.getByProductId(productId);

        // 유저 한 명당 이벤트 상품은 하나만 구매 가능.
        // 이 상품에 대해서 주문 상태가 COMPLETED인 주문이 있는지 검증
        if(orderProductRepository.existsCompletedEventOrder(productId,userId, OrderStatus.COMPLETED)){
            throw new CustomException(ErrorCode.EVENT_ORDER_ALREADY_EXISTS);

        }

        // 판매 중인 상품인지 확인
        if (product.getStatus() != ProductStatus.ON_SALE) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_ON_SALE);
        }

        // 재고 확인
        if (product.getStock() < quantity) {
            throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }


        // orderUid 생성
        String orderUid = IdFactory.generateWithDate("ORD", 8);


        // Order 저장 - orderId 발급
        Order order = Order.builder()
                .userId(userId)
                .orderUid(orderUid)
                .totalAmount(product.getPrice())
                .status(OrderStatus.COMPLETED)
                .build();

        Order savedOrder = orderRepository.save(order);

        // TODO :  재고 차감 락 적용
        productCommandService.decreaseStock(productId, quantity, savedOrder.getId());

        // OrderItem 저장 - 스냅샷
        orderProductRepository.save(OrderProduct.builder()
                .orderId(savedOrder.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(quantity)
                .build());

        // OrderUser 저장 - 스냅샷
        orderCommandService.saveOrderUser(savedOrder.getId(), user.getName(), user.getPhone(), user.getAddress());


        log.info("[이벤트 주문 생성] userId: {}, orderUid: {}, productId: {}",
                userId, orderUid, product.getId());

        return EventOrderResponse.from(orderUid, savedOrder.getStatus());

    }
}
