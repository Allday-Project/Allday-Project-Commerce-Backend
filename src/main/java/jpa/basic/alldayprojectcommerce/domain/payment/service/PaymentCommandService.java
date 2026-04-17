package jpa.basic.alldayprojectcommerce.domain.payment.service;

import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.request.CreatePaymentRequest;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.CreatePaymentResponse;
public interface PaymentCommandService {
    CreatePaymentResponse createPayment(String orderUid, CreatePaymentRequest request, LoginUserInfo loginUser);
}
