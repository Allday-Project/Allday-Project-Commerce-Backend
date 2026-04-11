package jpa.basic.alldayprojectcommerce.domain.refund.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly =true)
public class RefundQueryServiceImpl implements RefundQueryService {
}
