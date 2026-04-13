package jpa.basic.alldayprojectcommerce.common.security.auth.exception;

import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;

public class AuthUnauthenticatedException extends AuthException {
    public AuthUnauthenticatedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
