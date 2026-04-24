package jpa.basic.alldayprojectcommerce.domain.chat.repository;

import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    /**
     * 유저의 활성 채팅방 조회
     *
     * activeFlag = 1 방은 유저당 최대 1개
     */
    Optional<ChatRoom> findByUserIdAndActiveFlag(Long userId, Integer activeFlag);

    /**
     * 관리자 전용
     * 특정 상태의 방 전체 조회
     */
    List<ChatRoom> findByChatRoomStatusOrderByCreatedAtDesc(ChatRoomStatus status);

    /**
     * 관리자 전용
     * 전체 채팅방 조회
     */
    List<ChatRoom> findAllByOrderByCreatedAtDesc();
}
