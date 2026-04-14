package jpa.basic.alldayprojectcommerce.domain.order.service;

import jpa.basic.alldayprojectcommerce.common.factory.IdFactory;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.CreateOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderItem;
import jpa.basic.alldayprojectcommerce.domain.order.entity.OrderStatus;
import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import jpa.basic.alldayprojectcommerce.domain.user.entity.User;
import jpa.basic.alldayprojectcommerce.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderCommendServiceImpl implements OrderCommendService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    @Override
    public CreateOrderResponse create(Long userId, List<OrderItem> orderItems) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 사용자입니다."));

        List<Long> productIds = orderItems.stream()
                .map(OrderItem::productId)
                .toList();

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new IllegalStateException("존재하지 않는 상품이 포함되어 있습니다.");
        }

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        long totalAmount = orderItems.stream()
                .mapToLong(item -> productMap.get(item.productId()).getPrice() * item.quantity())
                .sum();

        String orderUid = IdFactory.generateWithDate("ORD", 8);

        Order order = Order.builder()
                .user(user)
                .orderUid(orderUid)
                .totalAmount(totalAmount)
                .status(OrderStatus.PENDING)
                .build();

        Order savedOrder = orderRepository.save(order);

        return new CreateOrderResponse(
                savedOrder.getOrderUid(),
                savedOrder.getTotalAmount()
        );
    }
}