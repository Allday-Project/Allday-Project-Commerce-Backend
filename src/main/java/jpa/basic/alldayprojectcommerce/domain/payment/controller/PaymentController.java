package jpa.basic.alldayprojectcommerce.domain.payment.controller;

import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.request.CreatePaymentRequest;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.CreatePaymentResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.service.PaymentCommandService;
import jpa.basic.alldayprojectcommerce.domain.payment.service.PaymentCommandServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders/{orderUid}/payments")
public class PaymentController {

    private PaymentCommandService paymentCommandService;

    public ResponseEntity<ApiResponse<CreatePaymentResponse>> createPayment(
            @PathVariable String orderUid,
            @RequestBody CreatePaymentRequest request
            ){
        CreatePaymentResponse response = paymentCommandService.createPayment(orderUid, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED,response));
    }

}
