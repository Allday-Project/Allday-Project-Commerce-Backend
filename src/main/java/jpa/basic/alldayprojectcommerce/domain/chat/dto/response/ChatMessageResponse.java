package jpa.basic.alldayprojectcommerce.domain.chat.dto.response;

import jpa.basic.alldayprojectcommerce.domain.chat.entity.ChatMessage;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.MessageType;
import jpa.basic.alldayprojectcommerce.domain.chat.entity.SenderType;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long senderId,
        SenderType senderType,
        MessageType messageType,
        String content,
        LocalDateTime createdAt
) {

    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getId(),
                chatMessage.getSenderId(),
                chatMessage.getSenderType(),
                chatMessage.getMessageType(),
                chatMessage.getContent(),
                chatMessage.getCreatedAt()
        );
    }
}
