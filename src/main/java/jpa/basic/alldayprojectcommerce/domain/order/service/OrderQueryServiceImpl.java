package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetAllOrdersResponse;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderItem;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderItemRepository;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
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
    private final OrderItemRepository orderItemRepository;
    private final OrderUserRepository orderUserRepository;
    private final UserQueryService userQueryService;

    /**
     * 주문 목록 조회
     * 최초 조회는 가장 최신부터 시작
     */
    @Override
    public CursorResponse<GetAllOrdersResponse> getAllOrders(LoginUserInfo loginUserInfo, Long cursorId, int size) {
        long cursor = (cursorId == null) ? Long.MAX_VALUE : cursorId;

        // size + 1개
        List<Order> orders = orderRepository.findByUserIdWithCursor(loginUserInfo.id(), cursor, size);

        return CursorResponse.of(
                orders.stream()
                        .map(order -> {
                            List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                            return GetAllOrdersResponse.from(order, items);
                        }).toList(),
                size,
                dto -> orderRepository.findByOrderUid(dto.orderUid())
                        .map(Order::getId)
                        .orElse(null)
        );
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

