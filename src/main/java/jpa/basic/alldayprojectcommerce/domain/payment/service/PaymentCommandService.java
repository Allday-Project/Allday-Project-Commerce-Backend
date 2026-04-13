package jpa.basic.alldayprojectcommerce.domain.payment.service;

import jakarta.transaction.Transactional;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfoDto;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.request.CreatePaymentRequest;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.CreatePaymentResponse;

@Transactional
public interface PaymentCommandService {
    CreatePaymentResponse createPayment(String orderUid, CreatePaymentRequest request, LoginUserInfoDto loginUser);
}
