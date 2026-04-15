package jpa.basic.alldayprojectcommerce.common.exception;

import jpa.basic.alldayprojectcommerce.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 예외 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleException(CustomException exception) {
        log.error("[API - ERROR] 발생 원인: ", exception);
        ErrorCode errorCode = exception.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    // @Valid 검증 실패 처리 — DTO에 @Valid 적용 시 검증 실패하면 자동 발생
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception) {

        log.error("[API - ERROR] 발생 원인: ", exception);
        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;
        List<FieldError> fieldErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::from)
                .toList();

        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode, fieldErrors));
    }
}