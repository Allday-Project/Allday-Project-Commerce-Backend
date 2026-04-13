package jpa.basic.alldayprojectcommerce.domain.payment.controller;

import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import jpa.basic.alldayprojectcommerce.domain.payment.service.PaymentCommandServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders/{orderUid}/payments")
public class PaymentController {

    private final PaymentCommandServiceImpl paymentCommandService;

    public ResponseEntity<ApiResponse<Void>> createPayment(
            @PathVariable String orderUid
    ){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED));
    }

}
