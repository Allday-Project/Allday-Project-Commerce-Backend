package jpa.basic.alldayprojectcommerce.domain.order.service.event;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.order.dto.response.EventOrderResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventOrderOptimisticLockServiceImpl implements EventOrderOptimisticLockService {

    private final EventOrderTransactionalService eventOrderTransactionalService;

    @Override
    public EventOrderResponse createEventOrderWithOptimisticLockRetry(Long productId, Long userId) {
        int maxRetryCount = 10;
        long backoffMillis = 50L;

        for (int attempt = 1; attempt <= maxRetryCount; attempt++) {
            try {
                return eventOrderTransactionalService.createEventOrderInNewTransaction(productId, userId);
            } catch (ObjectOptimisticLockingFailureException e) {
                log.warn("[OptimisticLock] 충돌 발생 productId={}, userId={}, attempt={}",
                        productId, userId, attempt);

                if (attempt == maxRetryCount) {
                    throw new CustomException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
                }

                sleep(backoffMillis);
            }
        }

        throw new CustomException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
    }

    private void sleep(long backoffMillis) {
        try {
            Thread.sleep(backoffMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.OPTIMISTIC_LOCK_CONFLICT);
        }
    }
}