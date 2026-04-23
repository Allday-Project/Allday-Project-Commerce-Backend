package jpa.basic.alldayprojectcommerce.application;

import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EventOrderConcurrencyTest {

    @Autowired
    private EventOrderFacade eventOrderFacade;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductQueryService productQueryService;

    @Autowired
    private OrderRepository orderRepository;

    private static final Long TEST_TICKET_PRODUCT_ID = 4L;
    private static final int TEST_TICKET_STOCK = 10;
    private static final int DEFAULT_TICKET_STOCK = 100;
    private static final int TOTAL_REQUEST_COUNT = 100;
    private static final int THREAD_POOL = 100;
    private static final long TEST_TIMEOUT_SECONDS = 20L;

    @AfterEach
    void tearDown() {
        clearOrders();
        restoreStock(DEFAULT_TICKET_STOCK);
    }

    @Test
    @DisplayName("락 없는 주문 생성 - 성능 및 정합성 테스트")
    void createEventOrder_withoutLock_fail() throws Exception {
        // given
        prepareTestData(TEST_TICKET_STOCK);

        // when
        ConcurrencyTestResult result = runConcurrencyTest(
                userId -> eventOrderFacade.createEventOrderWithoutLock(
                        TEST_TICKET_PRODUCT_ID,
                        userId
                )
        );

        // then
        Product product = getTestProduct();
        long orderCount = orderRepository.count();

        printResult("WithoutLock", result, orderCount, product.getStock());

        assertThat(result.completed()).isTrue();

        // 락 없는 버전은 정합성이 깨지는 것이 목적
        // 즉 정상 기대값(10/90/10/0)과 달라야 한다.
        boolean isExactlyCorrect =
                result.successCount() == 10 &&
                        result.failCount() == 90 &&
                        orderCount == 10 &&
                        product.getStock() == 0;

        assertThat(isExactlyCorrect).isFalse();
    }

    @Test
    @DisplayName("Redis 분산락 FailFast 전략 - 성능 및 정합성 테스트")
    void createEventOrder_redisLettuce_failFast() throws Exception {
        // given
        prepareTestData(TEST_TICKET_STOCK);

        // when
        ConcurrencyTestResult result = runConcurrencyTest(
                userId -> eventOrderFacade.createEventOrderWithRedisLockFailFast(
                        TEST_TICKET_PRODUCT_ID,
                        userId
                )
        );

        // then
        Product product = getTestProduct();
        long orderCount = orderRepository.count();

        printResult("Redis FailFast", result, orderCount, product.getStock());

        assertThat(result.completed()).isTrue();

        // FailFast는 락 획득 실패 시 즉시 종료 전략이므로
        // 성공은 0 또는 1 정도가 자연스럽다.
        assertThat(result.successCount()).isBetween(0, 1);
        assertThat(result.failCount()).isEqualTo(TOTAL_REQUEST_COUNT - result.successCount());

        // 주문 수도 성공 수와 같아야 한다.
        assertThat(orderCount).isEqualTo(result.successCount());

        // 재고는 최소 9, 최대 10이 자연스럽다.
        assertThat(product.getStock()).isBetween(9, 10);
    }

    @Test
    @DisplayName("Redis 분산락 Retry 전략 - 성능 및 정합성 테스트")
    void createEventOrder_redisLettuce_retry_success() throws Exception {
        // given
        prepareTestData(TEST_TICKET_STOCK);

        // when
        ConcurrencyTestResult result = runConcurrencyTest(
                userId -> eventOrderFacade.createEventOrderWithRedisLockRetry(
                        TEST_TICKET_PRODUCT_ID,
                        userId
                )
        );

        // then
        Product product = getTestProduct();
        long orderCount = orderRepository.count();

        printResult("Redis Retry", result, orderCount, product.getStock());

        assertThat(result.completed()).isTrue();
        assertThat(result.successCount()).isEqualTo(10);
        assertThat(result.failCount()).isEqualTo(90);
        assertThat(orderCount).isEqualTo(10);
        assertThat(product.getStock()).isEqualTo(0);
    }

    @Test
    @DisplayName("Redis 분산락 Blocking 전략 - 성능 및 정합성 테스트")
    void createEventOrder_redisLettuce_blocking_success() throws Exception {
        // given
        prepareTestData(TEST_TICKET_STOCK);

        // when
        ConcurrencyTestResult result = runConcurrencyTest(
                userId -> eventOrderFacade.createEventOrderWithRedisLockBlocking(
                        TEST_TICKET_PRODUCT_ID,
                        userId
                )
        );

        // then
        Product product = getTestProduct();
        long orderCount = orderRepository.count();

        printResult("Redis Blocking", result, orderCount, product.getStock());

        assertThat(result.completed()).isTrue();
        assertThat(result.successCount()).isEqualTo(10);
        assertThat(result.failCount()).isEqualTo(90);
        assertThat(orderCount).isEqualTo(10);
        assertThat(product.getStock()).isEqualTo(0);
    }

    /**
     * 공통 동시성 실행 로직
     */
    private ConcurrencyTestResult runConcurrencyTest(TestExecutor executor) throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL);

        CountDownLatch doneLatch = new CountDownLatch(TOTAL_REQUEST_COUNT);
        CyclicBarrier startBarrier = new CyclicBarrier(TOTAL_REQUEST_COUNT);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long startTime = System.nanoTime();

        for (int i = 0; i < TOTAL_REQUEST_COUNT; i++) {
            long userId = i + 1L;

            threadPool.submit(() -> {
                try {
                    startBarrier.await();

                    executor.execute(userId);

                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        boolean completed = doneLatch.await(TEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        threadPool.shutdown();

        long endTime = System.nanoTime();
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);

        return new ConcurrencyTestResult(
                successCount.get(),
                failCount.get(),
                elapsedMillis,
                completed
        );
    }

    /**
     * 테스트 시작 전 공통 준비
     */
    private void prepareTestData(int stock) {
        clearOrders();
        prepareStock(stock);
    }

    /**
     * 주문 데이터 초기화
     */
    private void clearOrders() {
        orderRepository.deleteAllInBatch();
    }

    /**
     * 테스트용 티켓 재고를 원하는 값으로 맞춘다.
     */
    private void prepareStock(int stock) {
        Product product = getTestProduct();
        int currentStock = product.getStock();

        if (currentStock > stock) {
            product.decreaseStock(currentStock - stock);
        } else if (currentStock < stock) {
            product.increaseStock(stock - currentStock);
        }

        productRepository.save(product);
    }

    /**
     * 테스트 종료 후 재고를 기본값으로 복구
     */
    private void restoreStock(int stock) {
        Product product = getTestProduct();
        int currentStock = product.getStock();

        if (currentStock > stock) {
            product.decreaseStock(currentStock - stock);
        } else if (currentStock < stock) {
            product.increaseStock(stock - currentStock);
        }

        productRepository.save(product);
    }

    /**
     * 테스트용 상품 조회
     */
    private Product getTestProduct() {
        return productQueryService.getByProductId(TEST_TICKET_PRODUCT_ID);
    }

    /**
     * 결과 출력
     */
    private void printResult(String label, ConcurrencyTestResult result, long orderCount, int stock) {
        System.out.println("===== 결과 : " + label + " =====");
        System.out.println("성공 수 = " + result.successCount());
        System.out.println("실패 수 = " + result.failCount());
        System.out.println("총 실행 시간(ms) = " + result.elapsedMillis());
        System.out.println("모든 요청 완료 여부 = " + result.completed());
        System.out.println("최종 주문 수 = " + orderCount);
        System.out.println("최종 재고 수 = " + stock);
        System.out.println("==============================");
    }

    @FunctionalInterface
    interface TestExecutor {
        void execute(Long userId);
    }

    record ConcurrencyTestResult(
            int successCount,
            int failCount,
            long elapsedMillis,
            boolean completed
    ) {}
}