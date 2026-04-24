package jpa.basic.alldayprojectcommerce.domain.chat.service;

import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatRoomResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;
import jpa.basic.alldayprojectcommerce.domain.chat.repository.ChatRoomRepository;
import jpa.basic.alldayprojectcommerce.domain.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatRoomQueryServiceImpl implements ChatRoomQueryService {

    private final ChatRoomRepository chatRoomRepository;

    private static final Integer ACTIVE_FLAG = 1;

    /**
     * 고객 본인의 활성화된 방 조회
     *
     * DB UNIQUE 제약으로 결과는 항상 0개 또는 1개
     * null 반환 -> 활성화된 방이 없다는 정상 흐름
     */
    @Override
    public ChatRoomResponse getMyActiveRoom(Long userId) {
        return chatRoomRepository.findByUserIdAndActiveFlag(userId, ACTIVE_FLAG)
                .map(ChatRoomResponse::from)
                .orElse(null);
    }

    /**
     * 채팅방 단건 조회
     *
     * 고객   -> 본인 소유 방
     * 관리자 -> 모든 방
     */
    @Override
    public ChatRoomResponse getRoom(Long roomId, Long userId, String role) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        validateAccess(chatRoom, userId, role);

        return ChatRoomResponse.from(chatRoom);
    }

    /**
     * 전체 채팅방 목록 조회 (관리자용)
     *
     * status == null -> 전체 조회
     * status != null -> 특정 상태만 필터링
     */
    @Override
    public Page<ChatRoomResponse> getAllRooms(ChatRoomStatus status, Pageable pageable) {
        return chatRoomRepository.findAllWithFilter(status, pageable);
    }

    private void validateAccess(ChatRoom chatRoom, Long userId, String role) {
        if (UserRole.ADMIN.name().equals(role)) {
            return;
        }
        if (!chatRoom.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }
    }
}
