package jpa.basic.alldayprojectcommerce.domain.order.entity;

public enum OrderStatus {
    PENDING,                // 결제 대기
    PAID,                   // 결제 완료
    DELIVERY_COMPLETED,     // 배송 완료
    CANCELLED,              // 주문 취소
    REFUNDED,               // 환불 완료
    REFUND_REQUESTED,       // 환불 요청
    CONFIRMED               // 구매 확정
}
