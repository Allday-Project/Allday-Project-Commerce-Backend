package jpa.basic.alldayprojectcommerce.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SenderType senderType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder

    public ChatMessage(Long roomId, Long senderId, SenderType senderType, String content, MessageType messageType) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = LocalDateTime.now();
    }

    // 입장, 종료 알림 생성용 정적 팩토리
    public static ChatMessage systemMessage(Long roomId, String content) {
        return ChatMessage.builder()
                .roomId(roomId)
                .senderId(null)
                .senderType(SenderType.SYSTEM)
                .content(content)
                .messageType(MessageType.SYSTEM)
                .build();
    }
}
