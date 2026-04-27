package jpa.basic.alldayprojectcommerce.domain.chat.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatRoomResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatMessage;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatMessageRepository;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRoomCreator {

    private final ChatRoomRepository    chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    private static final Integer ACTIVE_FLAG = 1;

    /**
     * REQUIRES_NEW — 별도 클래스에서 호출해야 프록시가 적용됨
     *
     * 같은 클래스 내부 호출은 프록시를 우회하므로 반드시 외부 빈에서 호출해야 함
     * DataIntegrityViolationException 발생 시 이 트랜잭션만 롤백
     * 부모 트랜잭션 세션은 오염되지 않음
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChatRoomResponse createNewRoom(Long userId, String title) {
        // try-catch 제거 — 예외를 그대로 위로 전파
        // REQUIRES_NEW 트랜잭션만 롤백되고 세션 폐기됨
        ChatRoom newRoom = ChatRoom.builder()
                .userId(userId)
                .title(title)
                .build();

        ChatRoom saved = chatRoomRepository.save(newRoom);

        chatMessageRepository.save(
                ChatMessage.systemMessage(saved.getId(), "상담원을 연결 중입니다...")
        );

        log.info("[채팅방] 신규 생성 userId: {}, roomId: {}", userId, saved.getId());
        return ChatRoomResponse.from(saved);
    }
}