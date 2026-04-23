package jpa.basic.alldayprojectcommerce.domain.chat.entity;

import lombok.Getter;

@Getter
public enum MessageType {
    CHAT,   // 일반 대화
    SYSTEM  // 시스템 알림
}
