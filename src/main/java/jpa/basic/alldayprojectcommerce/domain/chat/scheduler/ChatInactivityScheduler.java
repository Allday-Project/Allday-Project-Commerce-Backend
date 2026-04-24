package jpa.basic.alldayprojectcommerce.domain.chat.scheduler;

import jpa.basic.alldayprojectcommerce.common.lock.repository.RedisLockRepository;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatMessageResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.MessageType;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.SenderType;
import jpa.basic.alldayprojectcommerce.domain.chat.redis.ChatRedisPublisher;
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

    private static final String INACTIVITY_LOCK_KEY = "lock:chat:inactivity";
    private static final long INACTIVITY_LOCK_TTL = 55L;    // 55초

    /**
     * 비활성 채팅방 자동 종료 스케쥴러
     *
     * 실행 주기: 1분마다
     * 종료 기준: lastMessageAt 이후 10분 경과
     *
     * 처리 순서:
     *  1. 비활성 방 목록 조회 (READ - 락 없음)
     *  2. 각 방에 대해:
     *      a. autoCloseRoom() - 비관적 락 + 상태 변경 + 시스템 메시지 DB 저장
     *      b. Redis Pub/Sub으로 WebSocket 알림
     *  3. 개별 try-catch - 한 방 실패해도 나머지 방은 계속 처리
     *
     *  서버 2대 동시 실행 시 같은 방을 중복 처리할 수 있음
     *  현재는 autoCloseRoom() 내부 비관적 락 + 상태 재확인으로 방어
     *  LockService 완성 후 스케쥴러 진입 시점에 분산락 적용 예정
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

            List<ChatRoom> targets = chatRoomRepository.findInactiveRooms(cutOff);

            if (targets.isEmpty()) {
                log.debug("[자동종료] 처리 대상 없음");
                return;
            }

            log.info("[자동종료] 처리 대상 {}건");

            for (ChatRoom room : targets) {
                try {
                    Long roomId = room.getId();

                    // 비관적 락 + 상태 변경 + 시스템 메시지 DB 저장
                    chatRoomCommandService.autoCloseRoom(roomId);

                    /**
                     * WebSocket으로 실시간 알림
                     *
                     * autoCloseRoom()에서 저장한 시스템 메시지를 Redis를 통해 발행
                     * -> 모든 서버의 구독자에게 브로드캐스트
                     * -> 클라이언트가 messageType = SYSTEM 감지 -> 채팅창 비활성화 처리
                     *
                     * ChatMessageResponse를 직접 생성하는 이유:
                     * autoCloseRoom()은 @Transcational 메서드라서 반환값 없이 커밋만 함
                     * 알림용 DTO는 스케쥴러에서 직접 만들어서 발행
                     */
                    ChatMessageResponse notification = new ChatMessageResponse(
                            null,   // id - 알림 전용이라 null
                            null,      // senderId - 시스템 메시지
                            SenderType.SYSTEM,
                            MessageType.SYSTEM,
                            "10분간 응답이 없어 상담이 자동 종료되었습니다.",
                            LocalDateTime.now()
                    );

                    chatRedisPublisher.publish(roomId, notification);

                    log.info("[자동종료] 알림 발송 완료 roomId: {}", roomId);

                } catch (Exception e) {
                    // 한 방 실패해도 다른 방 처리 계속 진행
                    log.error("[자동종료] 처리 실패 roomId: {}", room.getId(), e);
                }
            }
        } finally {
            redisLockRepository.unlock(INACTIVITY_LOCK_KEY, lockValue);
        }
    }
}
