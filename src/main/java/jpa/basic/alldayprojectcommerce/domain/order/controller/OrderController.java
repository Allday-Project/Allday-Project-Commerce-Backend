package jpa.basic.alldayprojectcommerce.domain.order.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.order.dto.request.CreateOrderRequest;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.CreateOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetAllOrdersResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetOneOrderResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.GetOrderDetailsResponse;
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

    /**
     * 주문서 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestBody @Valid CreateOrderRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, orderCommandService.createOrder(loginUserInfo.id(), request)));
    }

    /**
     * 주문 목록 조회 - 커서 기반 무한 스크롤
     * cursorId가 없으면 가장 최신 주문부터 출력
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorResponse<GetAllOrdersResponse>>> getAllOrders(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK, orderQueryService.getAllOrders(loginUserInfo.id(), cursorId, size)));
    }

    /**
     * 주문서 조회 - 결제 전 화면
     * 유저 정보(name, phone, address)가 null일 수 있다.
     */
    @GetMapping("/{orderUid}")
    public ResponseEntity<ApiResponse<GetOneOrderResponse>> getOneOrder(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable String orderUid) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK, orderQueryService.getOneOrder(loginUserInfo.id(), orderUid)));
    }

    /**
     * 주문 상세 조회 - 결제 완료 후 화면
     * OrderUser 스냅샷 기반으로 주문 당시 고객 정보 반환
     */
    @GetMapping("/{orderUid}/details")
    public ResponseEntity<ApiResponse<GetOrderDetailsResponse>> getOrderDetails(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable String orderUid) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK, orderQueryService.getOrderDetails(loginUserInfo.id(), orderUid)));
    }

}
