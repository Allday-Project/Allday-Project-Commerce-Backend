package jpa.basic.alldayprojectcommerce.domain.order.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.exception.ApiResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.request.CreateOrderRequest;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.CreateOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.order.service.OrderCommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderCommendService commendService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> create(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse response = commendService.create(request.userId(), request.orderItems());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, response));
    }

    // TODO 전체 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getAll() {
        return null;
    }

    // TODO 단건 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> get(@RequestParam String id) {
        return null;
    }
}