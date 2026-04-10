package jpa.basic.alldayprojectcommerce.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    //   권한 에러 코드 (E###)
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "E001", "접근 권한이 없습니다."),

    //   공통 에러 코드 (A###)
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "A001", "입력값이 올바르지 않습니다."),

    //   사용자 관련 에러 코드 (U###)
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U001", "이미 존재하는 이메일입니다."),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "U002", "해당 유저는 존재하지 않습니다."),

    //   파일 관련 에러 코드 (F###)
    FILE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "F001", "파일 업로드를 실패했습니다."),

    //   프로필 관련 에러 코드 (P###)
    PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "프로필을 찾을 수 없습니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}