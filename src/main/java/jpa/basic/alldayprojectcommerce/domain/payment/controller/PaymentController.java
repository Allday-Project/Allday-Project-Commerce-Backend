package jpa.basic.alldayprojectcommerce.domain.payment.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUser;
import jpa.basic.alldayprojectcommerce.common.security.auth.LoginUserInfoDto;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.request.CreatePaymentRequest;
import jpa.basic.alldayprojectcommerce.domain.payment.dto.response.CreatePaymentResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.service.PaymentCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders/{orderUid}/payments")
public class PaymentController {

    private final PaymentCommandService paymentCommandService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreatePaymentResponse>> createPayment(
            @PathVariable
            @NotBlank
            @Size(min = 10, max = 30)
            @Pattern(regexp = "^[0-9a-zA-Z]+$")
            String orderUid,
            @Valid @RequestBody CreatePaymentRequest request,
            @LoginUser LoginUserInfoDto loginUser
            ){
        CreatePaymentResponse response = paymentCommandService.createPayment(orderUid, request,loginUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED,response));
    }

}
