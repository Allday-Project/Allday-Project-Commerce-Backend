package jpa.basic.alldayprojectcommerce.domain.chat.repository;

import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatRoomResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatRoomRepositoryCustom {

    /**
     * 관리자 전용
     * 전체 채팅방 조회
     */
    Page<ChatRoomResponse> findAllWithFilter(ChatRoomStatus status, Pageable pageable);
}
