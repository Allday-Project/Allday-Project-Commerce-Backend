package jpa.basic.alldayprojectcommerce.domain.chat.service;

import jpa.basic.alldayprojectcommerce.domain.chat.dto.request.CreateChatRoomRequest;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatRoomResponse;

import java.util.List;

public interface ChatRoomCommandService {

    /**
     * 채팅방 생성 또는 기존 활성 방 반환
     *
     * 활성 채팅방이 이미 있으면 기존 방 반환 (중복 생성 X)
     * 없으면 신규 생성
     */
    ChatRoomResponse createOrGetActiveRoom(Long userId, CreateChatRoomRequest request);

    /**
     * 채팅방 종료 (COMPLETED 상태로 전환)
     *
     * 고객/관리자 모두 호출 가능
     * WAITING / IN_PROGRESS -> COMPLETED
     * active_flag -> null 전환
     */
    void closeChatRoom(Long userId, Long roomId, String role);

    /**
     * 관리자 상담 시작
     *
     * WAITING -> IN_PROGRESS 전환
     * ADMIN 권한 필수
     */
    void joinChatRoom(Long adminId, Long roomId, String role);

    /**
     * 배치 자동 종료 - 스케쥴러 전용
     * bulkCompleteRooms + saveAll을 하나의 트랜잭션으로 묶어 원자성 보장
     */
    void batchAutoCloseRooms(List<Long> roomIds, String closeMessage);
}
