package jpa.basic.alldayprojectcommerce.domain.chat.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "chat_messages",
        indexes = {
                // 커서 기반 페이징 인덱스
                @Index(name = "idx_chat_messages_room_cursor", columnList = "room_id, id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SenderType senderType;

    @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다.")
    @Column(nullable = false, length = 1000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType;

    @Builder
    public ChatMessage(Long roomId, Long senderId, SenderType senderType, String content, MessageType messageType) {
        this.roomId = roomId;
        this.senderId = senderId;
        this.senderType = senderType;
        this.content = content;
        this.messageType = messageType;
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
