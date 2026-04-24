package jpa.basic.alldayprojectcommerce.domain.chat.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.request.CreateChatRoomRequest;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatRoomResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatMessage;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatMessageRepository;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatRoomRepository;
import jpa.basic.alldayprojectcommerce.domain.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatRoomCommandServiceImpl implements ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    // 매직 넘버 방지
    private static final Integer ACTIVE_FLAG = 1;

    /**
     * 활성화 상태 채팅방 1개 정책
     *
     * 1단계 — 앱 레벨: 기존 활성 방 조회 후 있으면 반환 (대부분 여기서 해결)
     * 2단계 — DB 레벨: UNIQUE(user_id, active_flag) 제약
     * 3단계 — 예외 처리: 동시 생성 시 DataIntegrityViolationException → 재조회
     */
    @Override
    public ChatRoomResponse createOrGetActiveRoom(Long userId, CreateChatRoomRequest request) {
        return chatRoomRepository.findByUserIdAndActiveFlag(userId, ACTIVE_FLAG)
                .map(existing -> {
                    log.info("[채팅방 기존 활성 방 반환 userId: {}, roomId: {}",
                                                userId, existing.getId());
                    return ChatRoomResponse.from(existing);
                }).orElseGet(() -> createNewRoom(userId, request.title()));
    }

    private ChatRoomResponse createNewRoom(Long userId, String title) {
        try {
            ChatRoom newRoom = ChatRoom.builder()
                    .userId(userId)
                    .title(title)
                    .build();

            ChatRoom savedChatRoom = chatRoomRepository.save(newRoom);

            chatMessageRepository.save(
                    ChatMessage.systemMessage(savedChatRoom.getId(), "상담원을 연결 중입니다...")
            );

            log.info("[채팅방] 신규 생성 userId: {}, roomId: {}", userId, savedChatRoom.getId());
            return ChatRoomResponse.from(savedChatRoom);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청 경합
            log.warn("[채팅방] 동시 생성 감지 -> 재조회 userId: {}", userId);
            return chatRoomRepository.findByUserIdAndActiveFlag(userId, ACTIVE_FLAG)
                    .map(ChatRoomResponse::from)
                    .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_ALREADY_EXISTS));
        }
    }

    @Override
    public void closeChatRoom(Long userId, Long roomId, String role) {
        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        validateAccess(chatRoom, userId, role);

        /**
         * WAITING / IN_PROGRESS -> COMPLETED
         * activeFlag = null 처리
         */
        chatRoom.changeStatus(ChatRoomStatus.COMPLETED);

        chatMessageRepository.save(
                ChatMessage.systemMessage(roomId, "상담이 종료되었습니다.")
        );

        log.info("[채팅방] 종료 userId: {}, roomId: {}, by: {}", userId, roomId, role);
    }

    @Override
    public void joinChatRoom(Long adminId, Long roomId, String role) {
        if (!UserRole.ADMIN.name().equals(role)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }

        ChatRoom chatRoom = chatRoomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        chatRoom.changeStatus(ChatRoomStatus.IN_PROGRESS);

        chatMessageRepository.save(
                ChatMessage.systemMessage(roomId, "상담원이 연결되었습니다.")
        );

        log.info("[채팅방] 상담 시작 adminId: {}, roomId: {}", adminId, roomId);
    }

    /**
     * 채팅방 접근 권한 검증
     *
     * 관리자      -> 모든 방 접근 가능
     * 일반 유저   -> 본인 소유 방만 접근 가능
     */
    private void validateAccess(ChatRoom chatRoom, Long userId, String role) {
        if (UserRole.ADMIN.name().equals(role)) {
            return;
        }
        if (!chatRoom.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }
    }
}
