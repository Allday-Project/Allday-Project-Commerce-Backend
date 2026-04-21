package jpa.basic.alldayprojectcommerce.domain.payment.dto.response;

import jpa.basic.alldayprojectcommerce.domain.payment.entity.PaymentStatus;

public record ConfirmPaymentResponse(
        String OrderUid,
        PaymentStatus status
) {
    public static ConfirmPaymentResponse of(String orderUid,PaymentStatus status){
        return new ConfirmPaymentResponse(orderUid, status);
    }
}
