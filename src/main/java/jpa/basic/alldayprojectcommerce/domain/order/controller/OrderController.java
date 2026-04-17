package jpa.basic.alldayprojectcommerce.domain.order.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.order.dto.request.CreateOrderRequest;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.CreateOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetAllOrdersResponse;
import jpa.basic.alldayprojectcommerce.domain.order.service.OrderCommandService;
import jpa.basic.alldayprojectcommerce.domain.order.service.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestBody @Valid CreateOrderRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, orderCommandService.createOrder(loginUserInfo, request)));
    }

    /**
     * 주문서 조회 - PENDING 상태 (결제 전 화면)
     * User 정보가 null이면 프론트에서 표시
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorResponse<GetAllOrdersResponse>>> getOrder(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK, orderQueryService.getAllOrders(loginUserInfo, cursorId, size)));
    }
}
