package jpa.basic.alldayprojectcommerce.application;

import jpa.basic.alldayprojectcommerce.domain.order.repository.OrderRepository;
import jpa.basic.alldayprojectcommerce.domain.product.entity.Product;
import jpa.basic.alldayprojectcommerce.domain.product.repository.ProductRepository;
import jpa.basic.alldayprojectcommerce.domain.product.service.ProductQueryService;
import jpa.basic.alldayprojectcommerce.domain.user.service.UserQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class EventOrderFacadeConcurrencyTest {

    /**
     * DummyDataProduct에서 티켓이 4번째로 저장된다는 전제로 사용하는 테스트용 상품 ID
     */
    private static final Long TEST_TICKET_PRODUCT_ID = 4L;

    /**
     * 동시에 주문 요청할 사용자 수
     */
    private static final int TOTAL_REQUEST_COUNT = 100;

    /**
     * 테스트 시나리오에서 사용할 티켓 재고 수
     */
    private static final int TEST_TICKET_STOCK = 10;

    @Autowired
    private EventOrderFacade eventOrderFacade;

    @Autowired
    private ProductQueryService productQueryService;

    @Autowired
    private UserQueryService userQueryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @AfterEach
    void tearDown() {
        // 테스트 중 생성된 주문 데이터만 정리
        orderRepository.deleteAllInBatch();

        // 다음 테스트를 위해 티켓 재고를 원래 더미값 100으로 복구
        Product ticketProduct = productRepository.findById(TEST_TICKET_PRODUCT_ID)
                .orElseThrow(() -> new IllegalArgumentException("테스트 종료 후 티켓 상품을 찾을 수 없습니다."));

        int currentStock = ticketProduct.getStock();

        // 현재 재고가 100보다 작으면 부족분만큼 복구
        if (currentStock < 100) {
            ticketProduct.increaseStock(100 - currentStock);
        }

        // 현재 재고가 100보다 크면 초과분만큼 차감
        if (currentStock > 100) {
            ticketProduct.decreaseStock(currentStock - 100);
        }

        productRepository.save(ticketProduct);
    }

    @Test
    @DisplayName("락 없는 주문 생성 - 재고 10개 티켓에 100명 동시 요청 시 정합성이 깨질 수 있다")
    void createEventOrderWithoutLock_concurrency_fail() throws Exception {

        // given

        // 테스트용 티켓 상품을 productId로 조회
        Product ticketProduct = productQueryService.getByProductId(TEST_TICKET_PRODUCT_ID);

        // 더미데이터 티켓 재고(100)를 테스트 시나리오용 재고(10)로 맞춘다.
        // 현재 재고가 10보다 많으면 초과분을 차감
        if (ticketProduct.getStock() > TEST_TICKET_STOCK) {
            ticketProduct.decreaseStock(ticketProduct.getStock() - TEST_TICKET_STOCK);
        }

        // 현재 재고가 10보다 적으면 부족분을 증가
        if (ticketProduct.getStock() < TEST_TICKET_STOCK) {
            ticketProduct.increaseStock(TEST_TICKET_STOCK - ticketProduct.getStock());
        }

        productRepository.save(ticketProduct);

        // 유저 1번 ~ 100번이 모두 존재하는지 사전 검증
        // DummyDataUser가 정상 적재되었다는 전제를 확인하는 용도
        for (long userId = 1L; userId <= TOTAL_REQUEST_COUNT; userId++) {
            userQueryService.getById(userId);
        }

        // 동시 실행용 스레드 풀
        ExecutorService executorService = Executors.newFixedThreadPool(100);

        // CountDownLatch : 여러 작업이 끝날 때까지 기다리는 도구
        // 모든 작업 종료를 기다리기 위한 래치
        CountDownLatch doneLatch = new CountDownLatch(TOTAL_REQUEST_COUNT);

        // CyclicBarrier : 모든 스레드를 동시에 출발시키기 위한 도구
        // 모든 스레드를 최대한 동시에 시작시키기 위한 배리어
        CyclicBarrier startBarrier = new CyclicBarrier(100);

        // 성공/실패 건수 집계용 카운터
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when

        for (int i = 0; i < TOTAL_REQUEST_COUNT; i++) {
            long userId = i + 1L;

            executorService.submit(() -> {
                try {
                    // 모든 스레드가 이 지점에 도달하면 동시에 시작
                    startBarrier.await();

                    // 같은 상품(productId)에 대해 서로 다른 사용자(userId)가 동시에 주문 요청
                    // 테스트 대상은 락이 없는 버전
                    eventOrderFacade.createEventOrderWithoutLock(
                            TEST_TICKET_PRODUCT_ID,
                            userId
                    );

                    // 예외 없이 주문 완료 시 성공 카운트 증가
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    // 재고 부족 등 예외 발생 시 실패 카운트 증가
                    failCount.incrementAndGet();

                } finally {
                    // 현재 작업 종료 표시
                    doneLatch.countDown();
                }
            });
        }

        // 모든 요청이 끝날 때까지 대기
        doneLatch.await();

        // 스레드 풀 종료
        executorService.shutdown();

        // 최종 상품 상태 재조회
        Product savedTicketProduct = productRepository.findById(TEST_TICKET_PRODUCT_ID)
                .orElseThrow(() -> new IllegalArgumentException("테스트 후 티켓 상품을 찾을 수 없습니다."));

        // 최종 주문 수 조회
        long orderCount = orderRepository.count();

        System.out.println("성공 주문 수 = " + successCount.get());
        System.out.println("실패 주문 수 = " + failCount.get());
        System.out.println("최종 주문 수 = " + orderCount);
        System.out.println("최종 재고 수 = " + savedTicketProduct.getStock());

        // then

        /**
         * 동시성 제어가 정상이라면 기대값은 아래와 같아야 한다.
         * - 성공 주문 수 = 10
         * - 실패 주문 수 = 90
         * - 최종 주문 수 = 10
         * - 최종 재고 수 = 0
         *
         * 하지만 지금은 락 없는 버전을 검증하는 테스트이므로
         * 이 기대값이 깨질 가능성이 높고,
         * 그 실패가 바로 동시성 문제가 존재한다는 증거다.
         */
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failCount.get()).isEqualTo(90);
        assertThat(orderCount).isEqualTo(10);
        assertThat(savedTicketProduct.getStock()).isEqualTo(0);
    }
}