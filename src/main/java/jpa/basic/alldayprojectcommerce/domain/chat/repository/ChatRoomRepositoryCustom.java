package jpa.basic.alldayprojectcommerce.domain.chat.repository;

import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatRoomResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRoomRepositoryCustom {

    /**
     * 관리자 전용
     * 전체 채팅방 조회
     */
    Page<ChatRoomResponse> findAllWithFilter(ChatRoomStatus status, Pageable pageable);

    /**
     * 자동 종료 대상 채팅방 조회
     *
     * 조건:
     * 1. lastMessageAt < cutoff (마지막 메시지 이후 일정 시간 경과)
     * 2. WAITING 또는 IN_PROGRESS 상태 (이미 종료된 상태)
     *
     * COMPLETED는 activeFlag=null이라서 조건 2로 자동 제외됨
     */
    List<ChatRoom> findInactiveRooms(LocalDateTime cutoff, Long lastId, int batchSize);

    /**
     * 배치 자동 종료 - Bulk Update
     *
     * 개별 autoCloseRoom() N번 호출 대신 IN 쿼리로 한 번에 상태 변경
     * DB 커넥션 N번 -> 1번으로 감소
     */
    void bulkCompleteRooms(List<Long> roomIds);
}
