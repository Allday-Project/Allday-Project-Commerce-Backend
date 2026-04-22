package jpa.basic.alldayprojectcommerce.domain.chat.entity;

import jakarta.persistence.*;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "chat_rooms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id @GeneratedValue
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus chatRoomStatus;

    @Column(nullable = false, length = 100)
    private String title;

    @Column
    private LocalDateTime lastMessageAt;

    @Builder
    public ChatRoom(Long userId, ChatRoomStatus chatRoomStatus, String title, LocalDateTime lastMessageAt) {
        this.userId = userId;
        this.chatRoomStatus = chatRoomStatus;
        this.title = title;
        this.lastMessageAt = lastMessageAt;
    }

    // 상태 전이 메서드
    public void changeStatus(ChatRoomStatus newStatus) {
        if (!this.chatRoomStatus.canTransitTo(newStatus)) {
            throw new CustomException(ErrorCode.CHAT_INVALID_STATUS_TRANSITION);
        }
        this.chatRoomStatus = newStatus;
    }

    // 마지막 메시지 시각 갱신
    public void updateLastMessageAt(LocalDateTime newLastMessageAt) {
        this.lastMessageAt = newLastMessageAt;
    }
}
