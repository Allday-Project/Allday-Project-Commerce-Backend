package jpa.basic.alldayprojectcommerce.domain.chat.dto.response;

import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoom;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatRoomStatus;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long id,
        Long userId,
        String title,
        ChatRoomStatus chatRoomStatus,
        LocalDateTime lastMessageAt,
        LocalDateTime createdAt
) {

    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return new ChatRoomResponse(
                chatRoom.getId(),
                chatRoom.getUserId(),
                chatRoom.getTitle(),
                chatRoom.getChatRoomStatus(),
                chatRoom.getLastMessageAt(),
                chatRoom.getCreatedAt()
        );
    }
}
