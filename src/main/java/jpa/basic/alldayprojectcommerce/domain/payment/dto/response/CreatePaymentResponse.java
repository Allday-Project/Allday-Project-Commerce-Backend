package jpa.basic.alldayprojectcommerce.domain.payment.dto.response;

import jpa.basic.alldayprojectcommerce.domain.payment.entity.Payment;

public record CreatePaymentResponse(
        String paymentUid
) {

    public static CreatePaymentResponse from(Payment payment) {
        return new CreatePaymentResponse(payment.getPaymentUid());
    }
}
