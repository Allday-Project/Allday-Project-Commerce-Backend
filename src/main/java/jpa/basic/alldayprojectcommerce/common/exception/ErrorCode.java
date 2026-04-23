package jpa.basic.alldayprojectcommerce.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //  인증/인가 에러 코드 (A###)
    AUTH_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "A001", "로그인 정보가 올바르지 않습니다."),
    AUTH_UNAUTHENTICATED_USER(HttpStatus.UNAUTHORIZED, "A002", "로그인 정보가 올바르지 않습니다."),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "유효하지 않은 Refresh Token 입니다."),
    AUTH_FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "A004", "접근 권한이 없습니다."),

    //  공통 에러 코드 (C###)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "입력값이 올바르지 않습니다."),
    DATABASE_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "서버가 응답하지 않습니다."),

    //  사용자 관련 에러 코드 (U###)
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U001", "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "해당 유저는 존재하지 않습니다."),
    USER_PASSWORD_NOT_MATCH(HttpStatus.UNAUTHORIZED, "U003", "현재 비밀번호가 일치하지 않습니다."),
    USER_PASSWORD_SAME_AS_CURRENT(HttpStatus.BAD_REQUEST, "U004", "현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다."),
    USER_ORDERER_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "U005", "결제를 위한 유저 정보가 필요합니다."),

    //  주문 관련 에러 코드 (O###)
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다."),
    ORDER_FORBIDDEN(HttpStatus.FORBIDDEN, "O002", "해당 주문에 접근할 권한이 없습니다."),
    ORDER_INVALID_STATUS(HttpStatus.BAD_REQUEST, "O003", "현재 주문 상태에서는 해당 작업을 수행할 수 없습니다."),
    ORDER_USER_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "O004", "이름, 전화번호, 주소를 입력해주세요."),
    ORDER_USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "O005", "주문자 정보를 찾을 수 없습니다."),
    ORDER_INVALID_UID(HttpStatus.BAD_REQUEST, "O005", "유효하지 않은 주문 UID 입니다."),
    ORDER_STATUS_NOT_PENDING(HttpStatus.BAD_REQUEST,"O006","주문 상태가 결제 대기 상태가 아닙니다."),
    EVENT_ORDER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST,"O007","이미 주문한 이벤트 상품입니다."),

    //  상품 관련 에러 코드(P###)
    ORDER_STATUS_NOT_COMPLETED(HttpStatus.BAD_REQUEST,"O007","주문 상태가 결제 완료 상태가 아닙니다."),
    ORDER_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "O008", "주문 상품이 존재하지 않습니다."),
    //   상품 관련 에러 코드(P###)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "해당 상품은 존재하지 않습니다."),
    PRODUCT_STOCK_LOCK_FAILED(HttpStatus.CONFLICT, "P002", "현재 요청이 많아 처리가 지연되고 있습니다. 잠시 후 다시 시도해 주세요."),
    PRODUCT_OUT_OF_STOCK(HttpStatus.CONFLICT, "P003", "재고가 부족합니다."),
    PRODUCT_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "P004", "주문할 수 없는 상품입니다."),

    //  장바구니 상품 관련 에러 코드(CP###)
    CARTPRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CP001", "해당 장바구니 상품은 존재하지 않습니다."),

    //  결제 도메인(PAY###)
    PAYMENT_ALREADY_SUCCESS(HttpStatus.BAD_REQUEST,"PAY001","해당 주문에 대하여 이미 성공한 결제 건이 존재합니다."),
    PAYMENT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PAY002", "올바르지 않은 결제 금액입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.BAD_REQUEST, "PAY003", "해당 결제 건을 찾을 수 없습니다."),
    PAYMENT_ORDER_NOT_MATCHES(HttpStatus.BAD_REQUEST, "PAY004", "입력한 주문에 대하여 생성된 결제 건이 아닙니다."),
    PAYMENT_INVALID_UID(HttpStatus.BAD_REQUEST, "PAY005", "유효하지 않은 결제 UID 입니다."),

    // 채팅 도메인(CH001)
    CHAT_INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "CH001", "유효하지 않은 상태 전이입니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CH002", "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_FORBIDDEN(HttpStatus.FORBIDDEN, "CH003", "채팅방에 접근할 권한이 없습니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "CH004", "이미 진행 중인 상담이 있습니다."),
    CHAT_ROOM_CLOSED(HttpStatus.BAD_REQUEST, "CH005", "종료된 상담방입니다."),
    CHAT_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "CH006", "채팅 인증에 실패했습니다."),
    CHAT_MESSAGE_EMPTY(HttpStatus.BAD_REQUEST, "CH007", "메시지 내용은 비어있을 수 없습니다."),
    CHAT_MESSAGE_TOO_LONG(HttpStatus.BAD_REQUEST, "CH008", "메시지는 1000자를 초과할 수 없습니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}