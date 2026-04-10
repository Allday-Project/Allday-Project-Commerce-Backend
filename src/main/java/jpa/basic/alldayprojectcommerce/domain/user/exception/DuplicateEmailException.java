package jpa.basic.alldayprojectcommerce.domain.user.exception;

import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;

public class DuplicateEmailException extends UserException {
    public DuplicateEmailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
