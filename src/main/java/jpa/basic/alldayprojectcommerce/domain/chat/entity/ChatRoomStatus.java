package jpa.basic.alldayprojectcommerce.domain.chat.entity;

import lombok.Getter;

@Getter
public enum ChatRoomStatus {
    WAITING("대기중"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료");

    private final String description;

    ChatRoomStatus(String description) {
        this.description = description;
    }

    // 유효한 상태 전이인지 검증
    public boolean canTransitTo(ChatRoomStatus nextStatus) {
        return switch (this) {
            case WAITING -> nextStatus == IN_PROGRESS || nextStatus == COMPLETED;
            case IN_PROGRESS -> nextStatus == COMPLETED;
            case COMPLETED -> false;
        };
    }
}
