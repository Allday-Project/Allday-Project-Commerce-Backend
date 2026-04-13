package jpa.basic.alldayprojectcommerce.common.exception;

import lombok.Getter;
import org.springframework.boot.autoconfigure.graphql.GraphQlProperties;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //   인증 에러 코드 (R###) - Auth 도메인
    AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "로그인 정보가 올바르지 않습니다."),
    AUTH_UNAUTHENTICATED_USER(HttpStatus.UNAUTHORIZED, "R002", "로그인 정보가 올바르지 않습니다."),

    //   권한 에러 코드 (E###)
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "E001", "접근 권한이 없습니다."),

    //   공통 에러 코드 (A###)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "A001", "입력값이 올바르지 않습니다."),

    //   사용자 관련 에러 코드 (U###) - 유저 도메인
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U001", "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "해당 유저는 존재하지 않습니다."),
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "U003", "프로필을 찾을 수 없습니다."),

    //   파일 관련 에러 코드 (F###)
    FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "F001", "파일 업로드를 실패했습니다."),

    // 상품 도메인 (P###)




    // 주문 도메인 (O###)
    ORDER_STATUS_NOT_PENDING(HttpStatus.BAD_REQUEST,"O001","주문 상태가 결제 대기 상태가 아닙니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND,"O002","주문 정보를 찾을 수 없습니다."),



    // 결제 도메인(PAY###)
    PAYMENT_ALREADY_SUCCESS(HttpStatus.BAD_REQUEST,"PAY001","해당 주문에 대하여 이미 성공한 결제 건이 존재합니다."),
    PAYMENT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PAY002", "올바르지 않은 결제 금액입니다."),



;


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}