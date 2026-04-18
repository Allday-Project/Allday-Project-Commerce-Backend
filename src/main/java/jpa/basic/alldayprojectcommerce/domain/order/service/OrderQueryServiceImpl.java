package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetAllOrdersResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetOneOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetOrderDetailsResponse;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderProduct;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderUser;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderProductRepository;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderRepository;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderUserRepository;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderUserRepository orderUserRepository;
    private final UserQueryService userQueryService;

    /**
     * 주문 목록 조회
     * 최초 조회는 가장 최신부터 시작
     */
    @Override
    public CursorResponse<GetAllOrdersResponse> getAllOrder(Long loginId, Long cursorId, int size) {
        long cursor = (cursorId == null) ? Long.MAX_VALUE : cursorId;

        // size + 1개
        List<Order> orders = orderRepository.findByUserIdWithCursor(loginId, cursor, size);

        return CursorResponse.of(
                orders.stream()
                        .map(order -> {
                            List<OrderProduct> items = orderProductRepository.findByOrderId(order.getId());
                            return GetAllOrdersResponse.from(order, items);
                        }).toList(),
                size,
                dto -> orderRepository.findByOrderUid(dto.orderUid())
                        .map(Order::getId)
                        .orElse(null)
        );
    }

    /**
     * 주문서 조회
     * 유저 정보(name, phone, address)가 null일 수 있다.
     */
    @Override
    public GetOneOrderResponse getOneOrder(Long loginId, String orderUid) {
        Order order = findOrderWithOwnerCheck(loginId, orderUid);


        return null;
    }

    /**
     * 주문 상세 조회
     * 본인 주문 X -> 조회 불가
     */
    @Override
    public GetOrderDetailsResponse getOneOrderDetail(Long loginId, String orderUid) {
        Order order = findOrderWithOwnerCheck(loginId, orderUid);

        // PENDING 상태는 아직 결제 전으로 주문 상세가 없다.
        if (order.getStatus() == OrderStatus.PENDING) {
            throw new CustomException(ErrorCode.ORDER_INVALID_STATUS);
        }

        OrderUser orderUser = orderUserRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_USER_NOT_FOUND));

        return GetOrderDetailsResponse.from(
                order,
                orderUser,
                orderProductRepository.findByOrderId(order.getId())
        );
    }

    /**
     * 주문 본인 검증
     */
    private Order findOrderWithOwnerCheck(Long loginId, String orderUid) {
        Order order = orderRepository.findByOrderUid(orderUid)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUserId().equals(loginId)) {
            throw new CustomException(ErrorCode.ORDER_FORBIDDEN);
        }

        return order;
    }

    @Override
    public Order getOrderByOrderUid(String orderUid) {
        return orderRepository.findByOrderUid(orderUid).orElseThrow(
                () -> new CustomException(ErrorCode.ORDER_NOT_FOUND)
        );
    }

    @Override
    public Order getOrderByOrderUidForUpdate(String orderUid) {
        return orderRepository.findByOrderUidForUpdate(orderUid).orElseThrow(
                () -> new CustomException(ErrorCode.ORDER_NOT_FOUND)
        );
    }
}

