package jpa.basic.alldayprojectcommerce.domain.chat.service;

import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatRoomResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatRoomQueryService {

    /**
     * 고객 본인의 활성화된 채팅방 조회
     *
     * 고객은 활성 상태의 방(WAITING / IN_PROGRESS)만 조회 가능
     * 종료된 방(COMPLETED)은 조회 대상이 아님
     */
    ChatRoomResponse getMyActiveRoom(Long userId);

    /**
     * 채팅방 단건 조회
     *
     * 고객   -> 본인 소유 방
     * 관리자 -> 모든 방
     */
    ChatRoomResponse getRoom(Long roomId, Long userId, String role);

    /**
     * 전체 채팅방 목록 조회 (관리자용)
     *
     * status 파라미터로 상태 필터링 (null이면 전체)
     */
    Page<ChatRoomResponse> getAllRooms(ChatRoomStatus status, Pageable pageable);
}
