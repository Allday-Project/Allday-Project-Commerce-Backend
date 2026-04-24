package jpa.basic.alldayprojectcommerce.common.exception;

import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@Slf4j
@ControllerAdvice
public class WebSocketExceptionHandler {

    /**
     * WebSocket 전용 전역 예외 처리기
     *
     * 에러 응답 경로: /user/queue/errors
     * 예외 발생한 당사자 세션에만 전송
     * 클라이언트: SUBSCRIBE /user/queue/errors 구독 필요
     */
    @MessageExceptionHandler(CustomException.class)
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleCustomException(CustomException e) {
        log.warn("[WebSocket] 비즈니스 예외 - {}: {}", e.getErrorCode().getCode(), e.getErrorCode().getMessage());

        return new ChatErrorResponse(
                e.getErrorCode().getCode(),
                e.getErrorCode().getMessage()
        );
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser("/queue/errors")
    public ChatErrorResponse handleUnexpectedException(Exception e) {
        log.error("[WebSocket] 예상치 못한 예외", e);

        return new ChatErrorResponse("SYS001", "서버 내부 오류가 발생했습니다.");
    }
}
