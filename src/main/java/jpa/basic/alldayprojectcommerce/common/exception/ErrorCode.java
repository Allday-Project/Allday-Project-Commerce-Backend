package jpa.basic.alldayprojectcommerce.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //   인증 에러 코드 (R###)
    AUTH_USER_NOT_FOUND(HttpStatus.UNAUTHORIZED, "R001", "로그인 정보가 올바르지 않습니다."),
    AUTH_UNAUTHENTICATED_USER(HttpStatus.UNAUTHORIZED, "R002", "로그인 정보가 올바르지 않습니다."),
    AUTH_INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "R003", "유효하지 않은 Refresh Token 입니다."),

    //   권한 에러 코드 (E###)
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "E001", "접근 권한이 없습니다."),

    //   공통 에러 코드 (A###)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "A001", "입력값이 올바르지 않습니다."),
    DATABASE_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A003", "서버가 응답하지 않습니다."),

    //   사용자 관련 에러 코드 (U###)
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U001", "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "해당 유저는 존재하지 않습니다."),

    //   파일 관련 에러 코드 (F###)
    FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "F001", "파일 업로드를 실패했습니다."),

    //   프로필 관련 에러 코드 (P###)
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "프로필을 찾을 수 없습니다."),

    //   상품 관련 에러 코드(A###)
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PR001", "해당 상품은 존재하지 않습니다."),
    STOCK_LOCK_FAILED(HttpStatus.CONFLICT, "PR002", "현재 요청이 많아 처리가 지연되고 있습니다. 잠시 후 다시 시도해 주세요."),
    PRODUCT_SOLD_OUT(HttpStatus.CONFLICT, "PR003", "품절된 상품입니다."),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "PR004", "재고가 부족합니다.");



    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}