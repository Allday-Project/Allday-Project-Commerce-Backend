package jpa.basic.alldayprojectcommerce.domain.refund.controller;

import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@NoArgsConstructor
@RequestMapping("/api/orders/{orderUid}/refunds")
public class RefundController {
}
