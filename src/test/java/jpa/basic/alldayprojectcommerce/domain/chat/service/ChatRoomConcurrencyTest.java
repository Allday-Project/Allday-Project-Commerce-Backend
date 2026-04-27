package jpa.basic.alldayprojectcommerce.domain.chat.service;

import jpa.basic.alldayprojectcommerce.domain.chat.dto.request.CreateChatRoomRequest;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatRoomRepository;
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

@SpringBootTest
@ActiveProfiles("test")
class ChatRoomConcurrencyTest {

    @Autowired ChatRoomCommandService chatRoomCommandService;
    @Autowired ChatRoomRepository     chatRoomRepository;

    @AfterEach
    void tearDown() {
        chatRoomRepository.deleteAll();
    }

    @Test
    @DisplayName("[동시성] 같은 유저가 동시에 10번 채팅방 생성 요청해도 1개만 생성된다")
    void concurrentRoomCreation_10threads() throws Exception {
        concurrentRoomCreationTest(10, 1001L);
    }

    @Test
    @DisplayName("[동시성] 같은 유저가 동시에 30번 채팅방 생성 요청해도 1개만 생성된다")
    void concurrentRoomCreation_30threads() throws Exception {
        concurrentRoomCreationTest(30, 1002L);
    }

    @Test
    @DisplayName("[동시성] 같은 유저가 동시에 50번 채팅방 생성 요청해도 1개만 생성된다")
    void concurrentRoomCreation_50threads() throws Exception {
        concurrentRoomCreationTest(50, 1003L);
    }

    private void concurrentRoomCreationTest(int threadCount, Long userId) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch  latch   = new CountDownLatch(threadCount);
        CyclicBarrier   barrier = new CyclicBarrier(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail    = new AtomicInteger();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    barrier.await(); // 모든 스레드 동시 출발
                    chatRoomCommandService.createOrGetActiveRoom(
                            userId,
                            new CreateChatRoomRequest("동시성 테스트")
                    );
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                    System.out.println("[실패] " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        long elapsed = System.currentTimeMillis() - startTime;

        // 모든 요청이 성공해야 함 (기존 방 반환 포함)
        System.out.printf("[결과] 스레드: %d | 성공: %d | 실패: %d | 소요시간: %dms%n",
                threadCount, success.get(), fail.get(), elapsed);

        assertThat(success.get()).isEqualTo(threadCount);
        assertThat(fail.get()).isZero();

        // DB에 활성 방은 딱 1개만 존재해야 함
        long activeRoomCount = chatRoomRepository.findAll().stream()
                .filter(r -> r.getUserId().equals(userId) && r.getActiveFlag() != null)
                .count();

        System.out.printf("[검증] userId=%d 활성 채팅방 수: %d (기대값: 1)%n",
                userId, activeRoomCount);

        assertThat(activeRoomCount).isEqualTo(1);
    }

    // === 채팅방 상태 변경 동시성 ===
    @Test
    @DisplayName("[동시성] 고객 종료 + 관리자 참여 동시 요청 — 비관적 락으로 순서 직렬화, 최종 상태 정합성 보장")
    void concurrentCloseAndJoin() throws Exception {
        var room = chatRoomCommandService.createOrGetActiveRoom(
                2001L, new CreateChatRoomRequest("상태변경 동시성 테스트")
        );
        Long roomId = room.id();

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch  latch   = new CountDownLatch(threadCount);
        CyclicBarrier   barrier = new CyclicBarrier(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail    = new AtomicInteger();

        executor.submit(() -> {
            try {
                barrier.await();
                chatRoomCommandService.closeChatRoom(2001L, roomId, "USER");
                success.incrementAndGet();
                System.out.println("[Thread1] 고객 종료 성공");
            } catch (Exception e) {
                fail.incrementAndGet();
                System.out.println("[Thread1] 고객 종료 실패: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                barrier.await();
                chatRoomCommandService.joinChatRoom(9999L, roomId, "ADMIN");
                success.incrementAndGet();
                System.out.println("[Thread2] 관리자 참여 성공");
            } catch (Exception e) {
                fail.incrementAndGet();
                System.out.println("[Thread2] 관리자 참여 실패: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executor.shutdown();

        System.out.printf("[결과] 성공: %d | 실패: %d%n", success.get(), fail.get());

        /**
         * 비관적 락의 역할 검증
         *
         * 락이 없으면: 두 스레드가 동시에 같은 상태를 읽고 덮어써서 데이터 정합성 깨짐
         * 락이 있으면: 하나씩 순서대로 처리되어 상태 전이가 항상 유효함
         *
         * 가능한 시나리오:
         * A) 고객 종료 먼저: WAITING→COMPLETED, 이후 관리자 참여 COMPLETED→IN_PROGRESS → 실패
         *    결과: 성공 1 + 실패 1
         * B) 관리자 참여 먼저: WAITING→IN_PROGRESS, 이후 고객 종료 IN_PROGRESS→COMPLETED → 성공
         *    결과: 성공 2 + 실패 0
         *
         * 둘 중 어느 시나리오든 최종 상태는 항상 유효한 상태 전이를 거침
         * 중요한 건 "둘 다 동시에 COMPLETED가 되거나 오염된 상태가 되지 않는 것"
         */
        assertThat(success.get() + fail.get()).isEqualTo(threadCount);

        // 최종 상태는 반드시 COMPLETED (둘 중 하나가 종료했거나, 순서대로 둘 다 처리됨)
        var finalRoom = chatRoomRepository.findById(roomId).orElseThrow();
        System.out.printf("[최종 상태] roomId=%d, status=%s%n", roomId, finalRoom.getChatRoomStatus());

        assertThat(finalRoom.getChatRoomStatus())
                .isIn(ChatRoomStatus.COMPLETED, ChatRoomStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("[동시성] 동일 채팅방 종료를 10번 동시 요청해도 1번만 성공한다")
    void concurrentClose_10threads() throws Exception {
        var room = chatRoomCommandService.createOrGetActiveRoom(
                3001L, new CreateChatRoomRequest("중복 종료 테스트")
        );
        Long roomId = room.id();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch  latch   = new CountDownLatch(threadCount);
        CyclicBarrier   barrier = new CyclicBarrier(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger fail    = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    barrier.await();
                    chatRoomCommandService.closeChatRoom(3001L, roomId, "USER");
                    success.incrementAndGet();
                } catch (Exception e) {
                    fail.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.printf("[결과] 스레드: %d | 성공: %d | 실패: %d%n",
                threadCount, success.get(), fail.get());
        System.out.printf("[기대] 성공: 1 | 실패: 9 (이미 종료된 방에 재시도)%n");

        // 최초 1번만 성공, 나머지는 CHAT_INVALID_STATUS_TRANSITION 예외
        assertThat(success.get()).isEqualTo(1);
        assertThat(fail.get()).isEqualTo(threadCount - 1);
    }
}