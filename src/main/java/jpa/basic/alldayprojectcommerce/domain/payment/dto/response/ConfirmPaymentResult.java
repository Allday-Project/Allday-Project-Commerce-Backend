package jpa.basic.alldayprojectcommerce.domain.payment.dto.response;

import jpa.basic.alldayprojectcommerce.domain.payment.entity.PaymentStatus;

public record ConfirmPaymentResult(
        PaymentStatus paymentStatus,
        boolean newlyConfirmed
) {

    public static ConfirmPaymentResult newlyConfirmed(PaymentStatus paymentStatus) {
        return new ConfirmPaymentResult(paymentStatus, true);
    }

    public static ConfirmPaymentResult alreadyProcessed(PaymentStatus paymentStatus) {
        return new ConfirmPaymentResult(paymentStatus, false);
    }
}