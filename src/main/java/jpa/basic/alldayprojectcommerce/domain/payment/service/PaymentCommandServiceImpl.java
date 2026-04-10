package jpa.basic.alldayprojectcommerce.domain.payment.service;

import jpa.basic.alldayprojectcommerce.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentCommandServiceImpl implements PaymentCommandService{
    private final PaymentRepository paymentRepository;
}
