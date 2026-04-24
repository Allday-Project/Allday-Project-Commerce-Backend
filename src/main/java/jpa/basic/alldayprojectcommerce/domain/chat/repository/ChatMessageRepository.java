package jpa.basic.alldayprojectcommerce.domain.chat.repository;

import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 특정 방의 메시지 최신순 조회
    List<ChatMessage> findByRoomIdOrderByIdDesc(Long roomId);

    // 커서 기반 페이징 - 특정 메시지 ID 이전 메시지들 조회
    List<ChatMessage> findByRoomIdAndIdLessThanOrderByIdDesc(Long roomId, Long cursorId);
}
