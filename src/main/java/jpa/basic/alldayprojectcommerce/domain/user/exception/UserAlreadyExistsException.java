package jpa.basic.alldayprojectcommerce.domain.user.exception;

import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;

public class UserAlreadyExistsException extends UserException {
    public UserAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
