package jpa.basic.alldayprojectcommerce.domain.chat.scheduler;

import jpa.basic.alldayprojectcommerce.common.lock.repository.RedisLockRepository;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatMessageResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.MessageType;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.SenderType;
import jpa.basic.alldayprojectcommerce.domain.chat.redis.ChatRedisPublisher;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatMessageRepository;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatRoomRepository;
import jpa.basic.alldayprojectcommerce.domain.chat.service.ChatRoomCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatInactivityScheduler {

    @Value("${chat.inactivity-timeout-minutes:10}")
    private int inactivityTimeoutMinutes;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomCommandService chatRoomCommandService;
    private final ChatRedisPublisher chatRedisPublisher;
    private final RedisLockRepository redisLockRepository;

    private final ChatMessageRepository chatMessageRepository;

    private static final String INACTIVITY_LOCK_KEY = "lock:chat:inactivity";
    private static final long INACTIVITY_LOCK_TTL = 55L;    // 55초
    private static final int BATCH_SIZE = 100;              // 한 번에 처리할 최대 방 수

    private String buildCloseMessage() {
        return String.format("%d분간 응답이 없어 상담이 자동 종료되었습니다.",
                                inactivityTimeoutMinutes);
    }

    /**
     * 비활성 채팅방 자동 종료 스케쥴러
     *
     * 실행 주기: 1분마다
     * 종료 기준: lastMessageAt 이후 inactivityTimeoutMinutes 경과
     *
     * 처리 순서:
     *  1. 분산락 획득 — 다른 서버 중복 실행 방지
     *  2. No-Offset 배치 조회 (BATCH_SIZE씩) — OOM 방지
     *  3. bulkCompleteRooms() — IN 쿼리로 상태 한 번에 변경
     *  4. saveAll() — 시스템 메시지 한 번에 저장
     *  5. Redis Pub/Sub — 방별 WebSocket 알림 발행
     *  6. Redis 발행 실패 시 DB는 이미 커밋됨 — 로그만 남기고 계속 진행
     */
    @Scheduled(fixedRate = 60_000) // 1분마다 실행
    public void closeInactiveRooms() {
        String lockValue = UUID.randomUUID().toString();

        if (!redisLockRepository.tryLock(INACTIVITY_LOCK_KEY, lockValue, INACTIVITY_LOCK_TTL)) {
            log.info("[자동종료] 다른 서버 실행 중 - 스킵");
            return;
        }

        try {
            // 기준 시각: 현재 - 10분
            LocalDateTime cutOff = LocalDateTime.now().minusMinutes(inactivityTimeoutMinutes);

            Long lastId = null;
            int totalProcessed = 0;

            while (true) {
                List<ChatRoom> targets = chatRoomRepository.findInactiveRooms(cutOff, lastId, BATCH_SIZE);

                if (targets.isEmpty()) break;

                List<Long> roomIds = targets.stream()
                        .map(ChatRoom::getId)
                        .collect(Collectors.toList());

                /**
                 * 상태 변경 + 메시지 저장을 하나의 트랜잭션으로 처리
                 */
                chatRoomCommandService.batchAutoCloseRooms(roomIds, buildCloseMessage());

                for (Long roomId : roomIds) {
                    try {
                        ChatMessageResponse notification = new ChatMessageResponse(
                                null,
                                null,
                                SenderType.SYSTEM,
                                MessageType.SYSTEM,
                                buildCloseMessage(),
                                LocalDateTime.now()
                        );
                        chatRedisPublisher.publish(roomId, notification);
                    } catch (Exception e) {
                        /**
                         * Redis 발행 실패해도 DB는 이미 커밋됨
                         * 클라이언트는 재연결 시 방 상태 조회로 2차 감지
                         */
                        log.error("[자동종료] 알림 실패 roomId: {}", roomId, e);
                    }
                }

                totalProcessed += targets.size();

                // 다음 배치 시작점 - 현재 배치의 마지막 ID
                lastId = targets.get(targets.size() - 1).getId();

                // 마지막 배치면 종료
                if (targets.size() < BATCH_SIZE) break;
            }

            if (totalProcessed > 0) {
                log.info("[자동종료] 총 {}건 처리 완료", totalProcessed);
            } else {
                log.debug("[자동종료] 처리 대상 없음");
            }

        } finally {
            redisLockRepository.unlock(INACTIVITY_LOCK_KEY, lockValue);
        }
    }
}
