package jpa.basic.alldayprojectcommerce.domain.order.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("결제 대기"),                // 결제 대기
    COMPLETED("결제 완료"),              // 결제 완료 (이 상태에서만 주문 취소 가능)
    DELIVERY_COMPLETED("배송 완료"),     // 배송 완료
    CANCELLED("주문 취소"),              // 주문 취소
    REFUNDED("환불 완료"),               // 환불 완료
    REFUND_REQUESTED("환불 요청"),       // 환불 요청
    CONFIRMED("구매 확정");              // 구매 확정 (이 상태 이후엔 환불만 가능)

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

}
