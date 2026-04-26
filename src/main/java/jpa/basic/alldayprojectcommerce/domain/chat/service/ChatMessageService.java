package jpa.basic.alldayprojectcommerce.domain.chat.service;

import jpa.basic.alldayprojectcommerce.common.CursorResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.request.ChatMessageRequest;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatMessageResponse;

public interface ChatMessageService {

    /**
     * 메시지 저장
     *
     * 저장 후 Response DTO로 반환 -> 서버가 다시 브로드캐스트할 때 사용
     */
    ChatMessageResponse saveMessage(Long roomId, Long senderId, String role, ChatMessageRequest request);

    /**
     * 메시지 조회 - 커서 기반 페이징
     *
     * cursorId == null -> 최초 조회
     * cursorId != null -> 해당 ID 이전 N개
     */
    CursorResponse<ChatMessageResponse> getMessages(
            Long roomId, Long userId, String role, Long cursorId, int size
    );
}
