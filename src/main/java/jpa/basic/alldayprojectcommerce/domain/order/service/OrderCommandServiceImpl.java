package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.common.util.IdFactory;
import jpa.basic.alldayprojectcommerce.domain.order.dto.request.CreateOrderRequest;
import jpa.basic.alldayprojectcommerce.domain.order.dto.request.OrderItemRequest;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.CreateOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderItem;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderItemRepository;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderRepository;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderUserRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.entity.ProductStatus;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryService;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserQueryService;
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
    private final OrderItemRepository orderItemRepository;
    private final OrderUserRepository orderUserRepository;
    private final ProductQueryService productQueryService;
    private final UserQueryService userQueryService;

    /**
     * 주문서 생성
     *
     * @param loginUserInfo  : 인증된 사용자
     * @param request           : 주문 생성 요청 DTO
     * @return                  : 주문 생성 응답 DTO
     */
    @Override
    public CreateOrderResponse createOrder(LoginUserInfo loginUserInfo, CreateOrderRequest request) {
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
                .userId(loginUserInfo.id())
                .orderUid(orderUid)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        // OrderItem 저장 - 스냅샷
        for (int i = 0; i < request.orderItems().size(); i++) {
            OrderItemRequest itemRequest = request.orderItems().get(i);
            Product product = products.get(i);

            orderItemRepository.save(OrderItem.builder()
                    .orderId(savedOrder.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .productPrice(product.getPrice())
                    .quantity(itemRequest.quantity())
                    .build());
        }

        log.info("[주문 생성] userId: {}, orderUid: {}, totalAmount: {}",
                    loginUserInfo.id(), orderUid, totalAmount);

        return new CreateOrderResponse(orderUid, totalAmount);
    }

    @Override
    public void confirmOrder(LoginUserInfo loginUserInfo, String orderUid) {

    }

    @Override
    public void cancelOrder(LoginUserInfo loginUserInfo, String orderUid) {

    }

    @Override
    public void saveOrderUser(Long orderId, String name, String phone, String address) {

    }

    @Override
    public void markOrderPaid(String orderUid) {

    }
}
