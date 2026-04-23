package jpa.basic.alldayprojectcommerce.domain.order.controller;

import jpa.basic.alldayprojectcommerce.application.EventOrderFacade;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventOrderController {
    private final EventOrderFacade eventOrderFacade;

    /*
        동시성 확인을 위한 이벤트 티켓 무료 나눔 API
        모바일 티켓이라고 가정, 배송비 X
        이벤트 카테고리에 있는 제품들은 상세 조회를 눌렀을 때
        장바구니 버튼이 없고 주문하기를 눌렀을 때 이 API가 바로 호출되는 것으로 프론트 구성한다고 가정
        상세 조회에서는 상품 상태(판매중, 품절, 단종)와 상품 재고 확인 가능
        장바구니 X, 주문서 X, 내 정보 수정 X 모두 거치지 않음
        주문 성공 시 주문 목록 조회로 리다이렉트
        주문 실패 시 '주문에 실패했습니다' 문구 띄우고 이벤트 상품 목록 조회로 리다이렉트
     */
    @PostMapping("/products/{productId}/orders")
    public ResponseEntity<ApiResponse<EventOrderResponse>> createEventOrder(
            @PathVariable Long productId,
            @RequestParam Long userId
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, eventOrderFacade.createEventOrderWithoutLock(productId, userId)));
    }

}
