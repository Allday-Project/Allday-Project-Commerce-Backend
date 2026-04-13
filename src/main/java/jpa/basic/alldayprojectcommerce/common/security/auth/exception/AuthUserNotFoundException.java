package jpa.basic.alldayprojectcommerce.common.security.auth.exception;

import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;

public class AuthUserNotFoundException extends AuthException {
    public AuthUserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
