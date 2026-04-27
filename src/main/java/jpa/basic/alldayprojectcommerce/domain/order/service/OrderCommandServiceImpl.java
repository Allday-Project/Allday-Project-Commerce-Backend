package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.util.IdFactory;
import jpa.basic.alldayprojectcommerce.domain.order.dto.request.CreateOrderRequest;
import jpa.basic.alldayprojectcommerce.domain.order.dto.request.OrderItemRequest;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.CreateOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderProduct;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderUser;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderProductRepository;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderRepository;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderUserRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OrderCommandServiceImpl implements OrderCommandService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderUserRepository orderUserRepository;
    private final ProductQueryService productQueryService;

    /**
     * 주문서 생성
     *
     * @param loginId : 인증된 사용자
     * @param request : 주문 생성 요청 DTO
     * @return : 주문 생성 응답 DTO
     */
    @Override
    public CreateOrderResponse createOrder(Long loginId, CreateOrderRequest request) {
        long totalAmount = 0L;

        List<Product> products = new ArrayList<>();

        // 상품 검증
        for (OrderItemRequest item : request.orderItems()) {
            Product product = productQueryService.getByProductId(item.productId());

            // 판매 중인 상품인지 확인
            if (product.getStatus() != ProductStatus.ON_SALE) {
                throw new CustomException(ErrorCode.PRODUCT_NOT_ON_SALE);
            }

            // 재고 확인
            if (product.getStock() < item.quantity()) {
                throw new CustomException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }

            totalAmount += product.getPrice() * item.quantity();
            products.add(product);
        }

        // orderUid 생성
        String orderUid = IdFactory.generateWithDate("ORD", 8);

        // Order 저장 - orderId 발급
        Order order = Order.builder()
                .userId(loginId)
                .orderUid(orderUid)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // OrderItem 저장 - 스냅샷
        List<OrderProduct> orderProducts = new ArrayList<>();

        for (int i = 0; i < request.orderItems().size(); i++) {
            OrderItemRequest itemRequest = request.orderItems().get(i);
            Product product = products.get(i);

            orderProducts.add(OrderProduct.builder()
                    .orderId(savedOrder.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(itemRequest.quantity())
                    .build());
        }

        orderProductRepository.saveAll(orderProducts);

        log.info("[주문 생성] userId: {}, orderUid: {}, totalAmount: {}",
                loginId, orderUid, totalAmount);

        return new CreateOrderResponse(orderUid, totalAmount);
    }

    @Override
    public void confirmOrder(Long loginId, String orderUid) {

    }

    @Override
    public void cancelOrder(Long loginId, String orderUid) {

    }

    @Override
    public void saveOrderUser(Long orderId, String name, String phone, String address) {
        // 결제 재시도로 이미 저장된 데이터가 있다면 중복 저장 방지
        if (orderUserRepository.findByOrderId(orderId).isPresent()) {
            log.info("[OrderUser] 이미 저장된 스냅샷 존재 orderId: {}", orderId);
            return;
        }

        orderUserRepository.save(
                OrderUser.builder()
                        .orderId(orderId)
                        .name(name)
                        .phone(phone)
                        .address(address)
                        .build()
        );

        log.info("[OrderUser] 스냅샷 저장 완료 orderId: {}", orderId);
    }

    @Override
    public void markOrderComplete(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new CustomException(ErrorCode.ORDER_STATUS_NOT_PENDING);
        }

        order.updateStatus(OrderStatus.COMPLETED);
        log.info("[주문 상태 변경] orderUid: {}, orderStatus: {}", order.getOrderUid(), order.getStatus());
    }

    @Override
    public void markOrderDelivered(Order order) {
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ORDER_STATUS_NOT_COMPLETED);
        }

        order.updateStatus(OrderStatus.DELIVERY_COMPLETED);
        log.info("[주문 상태 변경] orderUid: {}, orderStatus: {}", order.getOrderUid(), order.getStatus());
    }

    private Order findOrderWithOwnerCheck(Long loginId, String orderUid) {
        Order order = orderRepository.findByOrderUid(orderUid)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(loginId)) {
            throw new CustomException(ErrorCode.ORDER_FORBIDDEN);
        }

        return order;
    }
}
