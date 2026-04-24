package jpa.basic.alldayprojectcommerce.domain.chat.controller;

import jakarta.validation.Valid;
import jpa.basic.alldayprojectcommerce.common.config.StompPrincipal;
import jpa.basic.alldayprojectcommerce.common.exception.CustomException;
import jpa.basic.alldayprojectcommerce.common.exception.ErrorCode;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.request.ChatMessageRequest;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatMessageResponse;
import jpa.basic.alldayprojectcommerce.domain.chat.redis.ChatRedisPublisher;
import jpa.basic.alldayprojectcommerce.domain.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final ChatRedisPublisher chatRedisPublisher;

    /**
     * 클라이언트 -> 서버: 메시지 전송
     *
     * chatRedisPublisher.publish(roomId, response)
     *  1. Redis 채널에 발행
     *  2. 모든 서버의 ChatRedisSubscriber가 수신
     *  3. 각 서버가 자기 WebSocket 구독자에게 브로드캐스트
     *  4. 서버 A/B 어디 접속한 구독자든 메시지 수신 가능
     */
    @MessageMapping("/chat/{roomId}")
    public void sendMessage(
            @DestinationVariable Long roomId,
            @Valid ChatMessageRequest request,
            Principal principal) {

        if (!(principal instanceof StompPrincipal stompUser)) {
            log.warn("[WebSocket] Principal이 StompPrincipal이 아님 - 비정상");
            throw new CustomException(ErrorCode.CHAT_UNAUTHORIZED);
        }

        Long senderId = stompUser.getUserId();
        String role = stompUser.getRole();

        /**
         * 메시지 저장 + 권한 검증
         *
         * 검증 실패 시 클라이언트에 STOMP ERROR 프레임 전송
         */
        ChatMessageResponse response =
                chatMessageService.saveMessage(roomId, senderId, role, request);

        /**
         * Redis 채널에 발행
         *
         * 모든 서버의 구독자에게 브로드캐스트
         */

        chatRedisPublisher.publish(roomId, response);

        log.debug("[WebSocket] 메시지 발행 roomId: {}, messageId: {}",
                                                roomId, response.id());
    }
}
