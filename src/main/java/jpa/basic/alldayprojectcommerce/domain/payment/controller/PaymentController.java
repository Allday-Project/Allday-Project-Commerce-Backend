package jpa.basic.alldayprojectcommerce.domain.payment.controller;

import jpa.basic.alldayprojectcommerce.domain.payment.service.PaymentCommandServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/orders/{orderUid}/payments")
public class PaymentController {

    private final PaymentCommandServiceImpl paymentService;

}
