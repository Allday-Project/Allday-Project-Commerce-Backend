package jpa.basic.alldayprojectcommerce.domain.user.exception;

import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;

public class UserNotFoundException extends UserException{
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}
