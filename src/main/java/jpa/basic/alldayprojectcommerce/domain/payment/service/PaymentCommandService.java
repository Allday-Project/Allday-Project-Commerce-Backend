package jpa.basic.alldayprojectcommerce.domain.payment.service;

import jpa.basic.alldayprojectcommerce.domain.order.entity.Order;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.request.CreatePaymentRequest;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.ConfirmPaymentResult;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.CreatePaymentResponse;

public interface PaymentCommandService {
    CreatePaymentResponse createPayment(String orderUid, CreatePaymentRequest request, Long loginUserId);

    ConfirmPaymentResult confirmPayment(Order order, String paymentUid);
}
