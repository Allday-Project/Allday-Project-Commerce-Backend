package jpa.basic.alldayprojectcommerce.domain.chat.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Redis 채널 메시지 수신 -> WebSocket 브로드캐스트
     *
     * 흐름:
     * 1. 어떤 서버에서든 "chat:room:{roomId}" 채널에 발행
     * 2. 모든 서버의 이 메서드가 호출됨
     * 3. 각 서버는 자신이 보유한 WebSocket 구독자에게 브로드캐스트
     *
     * "chat:room:".length() = 10 이후 문자열이 roomId
     */
    @Override
    public void onMessage(Message message, @Nullable byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String roomId = channel.substring("chat:room:".length());

            ChatMessageResponse response = objectMapper.readValue(
                    message.getBody(), ChatMessageResponse.class);

            // 구독자 전체에게 브로드캐스트
            simpMessagingTemplate.convertAndSend("/sub/chat/" + roomId, response);

            log.debug("[Redis Sub] roomId: {}, messageId: {}", roomId, response.id());

        } catch (Exception e) {
            log.error("[Redis Sub] 메시지 처리 실패", e);
        }
    }
}
