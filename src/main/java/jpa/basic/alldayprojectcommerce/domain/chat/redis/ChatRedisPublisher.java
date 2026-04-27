package jpa.basic.alldayprojectcommerce.domain.chat.redis;

import jpa.basic.alldayprojectcommerce.domain.chat.dto.response.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatRedisPublisher {

    private final RedisTemplate<String, Object> chatRedisTemplate;

    /**
     * Redis 채널에 채팅 메시지 발행
     *
     * 채널명: "chat:room:{roomId}"
     * 모든 서버의 ChatRedisSubscriber가 이 채널을 구독 중
     */
    public void publish(Long roomId, ChatMessageResponse message) {
        String channel = "chat:room:" + roomId;
        chatRedisTemplate.convertAndSend(channel, message);
        log.debug("[Redis Pub] channel: {}, messageId: {}", channel, message.id());
    }
}
