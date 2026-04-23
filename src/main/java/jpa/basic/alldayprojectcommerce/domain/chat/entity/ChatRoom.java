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
@Table(
        name = "chat_rooms",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_active",
                columnNames = {"user_id", "active_flag"}
        ),
        indexes = {
                @Index(name = "idx_chat_rooms_status", columnList = "chat_rooms_status"),
                @Index(name = "idx_chat_rooms_user_id", columnList = "user_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus chatRoomStatus;

    @Column(nullable = false, length = 100)
    private String title;

    /**
     * MySQL NULL 유니크 트릭
     *
     * UNIQUE(user_id, active_flag) -> null은 여러 개 허용, 1은 1개만 허용
     * 활성 상태(WAITING / IN_PROFRESS)면 1, 종료(COMPLETED)면 null
     * Boolean 사용 시 false에도 유니크 적용돼서 과거 상담 여러 개 불가
     */
    @Column(name = "active_flag")
    private Integer activeFlag;

    @Column
    private LocalDateTime lastMessageAt;

    @Builder
    public ChatRoom(Long userId, String title) {
        this.userId = userId;
        this.chatRoomStatus = ChatRoomStatus.WAITING;
        this.title = title;
        this.activeFlag = 1;
        this.lastMessageAt = LocalDateTime.now();
    }

    // 상태 전이 메서드
    public void changeStatus(ChatRoomStatus newStatus) {
        if (!this.chatRoomStatus.canTransitTo(newStatus)) {
            throw new CustomException(ErrorCode.CHAT_INVALID_STATUS_TRANSITION);
        }
        this.chatRoomStatus = newStatus;

        // COMPLETED로 전환 시 active_flag를 null로 설정
        if (newStatus == ChatRoomStatus.COMPLETED) {
            this.activeFlag = null;
        }
    }

    // 마지막 메시지 시각 갱신
    public void updateLastMessageAt(LocalDateTime newLastMessageAt) {
        // null이면 무시
        if (newLastMessageAt == null) return;

        // 미래 시각 방어
        if (newLastMessageAt.isAfter(LocalDateTime.now())) return;

        // 과거 시각 무시
        if (this.lastMessageAt != null && newLastMessageAt.isBefore(this.lastMessageAt)) return;

        this.lastMessageAt = newLastMessageAt;
    }
}
