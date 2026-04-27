package jpa.basic.alldayprojectcommerce.domain.chat.entity;

import lombok.Getter;

@Getter
public enum SenderType {
    CUSTOMER,   // 고객
    ADMIN,      // 관리자
    SYSTEM      // 입장, 종료 자동 메시지
}
