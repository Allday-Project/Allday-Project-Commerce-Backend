package jpa.basic.alldayprojectcommerce.common.security.auth.exception;

import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {
    private final ErrorCode errorCode;

    public AuthException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
