package jpa.basic.alldayprojectcommerce.domain.chat.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.request.ChatMessageRequest;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatMessageResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.*;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatMessageRepository;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatRoomRepository;
import jpa.basic.alldayprojectcommerce.domain.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;

    @Override
    @Transactional
    public ChatMessageResponse saveMessage(Long roomId, Long senderId, String role, ChatMessageRequest request) {
        // 채팅방 존재 여부
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        // 채팅방이 이미 종료된 상태인지
        if (chatRoom.getChatRoomStatus() == ChatRoomStatus.COMPLETED) {
            throw new CustomException(ErrorCode.CHAT_ROOM_CLOSED);
        }

        // 인가 검증 - 방 소유자 또는 관리자만 전송 가능
        SenderType senderType = validateAndGetSenderType(chatRoom, senderId, role);

        ChatMessage message = ChatMessage.builder()
                .roomId(roomId)
                .senderId(senderId)
                .senderType(senderType)
                .content(request.content())
                .messageType(MessageType.CHAT)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // 최근 활동 시각 추적용
        chatRoom.updateLastMessageAt(LocalDateTime.now());

        log.debug("[메시지 저장] roomId: {}, senderId: {}, type: {}"
                                , roomId, senderId, senderType);

        return ChatMessageResponse.from(savedMessage);
    }

    @Override
    public CursorResponse<ChatMessageResponse> getMessages(Long roomId, Long userId, String role, Long cursorId, int size) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        validateReadAccess(chatRoom, userId, role);

        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<ChatMessage> messages = (cursorId == null)
                ? chatMessageRepository.findByRoomIdOrderByIdDesc(roomId, pageRequest)
                : chatMessageRepository.findByRoomIdAndIdLessThanOrderByIdDesc(roomId, cursorId, pageRequest);

        List<ChatMessageResponse> rawContent = messages.stream()
                .map(ChatMessageResponse::from)
                .toList();

        return CursorResponse.of(rawContent, size, ChatMessageResponse::id);
    }

    /**
     * 메시지 전송 권한 검증 및 SenderType 결정
     *
     * 관리자      -> ADMIN
     * 방 소유자    -> CUSTOMER
     * 둘 다 아니면 예외
     */
    private SenderType validateAndGetSenderType(ChatRoom chatRoom, Long senderId, String role) {
        if (UserRole.ADMIN.name().equals(role)) {
            return SenderType.ADMIN;
        }
        if (chatRoom.getUserId().equals(senderId)) {
            return SenderType.CUSTOMER;
        }
        throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
    }

    private void validateReadAccess(ChatRoom chatRoom, Long userId, String role) {
        if (UserRole.ADMIN.name().equals(role)) {
            return;
        }
        if (!chatRoom.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }
    }
}
