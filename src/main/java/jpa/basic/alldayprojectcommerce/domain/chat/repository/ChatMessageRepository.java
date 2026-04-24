package jpa.basic.alldayprojectcommerce.domain.chat.repository;

import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 특정 방의 메시지 최신순 조회
     *
     * 인덱스(room_id, id)로 정렬
     */
    List<ChatMessage> findByRoomIdOrderByIdDesc(Long roomId, Pageable pageable);

    /**
     * 커서 기반 페이징 - 특정 메시지 ID 이전 메시지들 조회
     *
     * 채팅방 스크롤 업 -> 과거 메시지 추가 로드
     * id가 cursor보다 작은 것들 중 최신순 N개
     */
    List<ChatMessage> findByRoomIdAndIdLessThanOrderByIdDesc(Long roomId, Long cursorId, Pageable pageable);
}
