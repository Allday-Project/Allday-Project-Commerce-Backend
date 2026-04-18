package jpa.basic.alldayprojectcommerce.domain.payment.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.application.OrderPaymentFacade;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.request.CreatePaymentRequest;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.ConfirmPaymentResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.CreatePaymentResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.service.PaymentCommandService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders/{orderUid}/payments")
@Validated
public class PaymentController {

    private final PaymentCommandService paymentCommandService;
    private final OrderPaymentFacade orderPaymentFacade;

    @PostMapping
    public ResponseEntity<ApiResponse<CreatePaymentResponse>> createPayment(
            @PathVariable
            @NotBlank
            @Pattern(regexp = "^ORD-\\d{8}-[0-9a-zA-Z]{8}$",
                    message = "orderUid는 ORD-YYYYMMDD-XXXXXXXX 형식이어야 합니다."
            )
            String orderUid,
            @Valid @RequestBody CreatePaymentRequest request,
            @LoginUser LoginUserInfo loginUser
            ){
        CreatePaymentResponse response = paymentCommandService.createPayment(orderUid, request,loginUser.id());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED,response));
    }


    @PostMapping("/{paymentUid}/confirm")
    public ResponseEntity<ApiResponse<ConfirmPaymentResponse>> confirmPayment(
            @PathVariable
            @NotBlank
            @Pattern(regexp = "^ORD-\\d{8}-[0-9a-zA-Z]{8}$",
                    message = "orderUid는 ORD-YYYYMMDD-XXXXXXXX 형식이어야 합니다."
            )
            String orderUid,
            @PathVariable String paymentUid,
            @LoginUser LoginUserInfo loginUser){
        ConfirmPaymentResponse response = orderPaymentFacade.confirmOrderPayment(orderUid,paymentUid,loginUser.id());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK,response));
    }


}
