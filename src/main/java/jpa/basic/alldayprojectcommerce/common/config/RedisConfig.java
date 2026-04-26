package jpa.basic.alldayprojectcommerce.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jpa.basic.alldayprojectcommerce.domain.chat.redis.ChatRedisSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 키/값 모두 String 직렬화 — Redis CLI에서 사람이 읽을 수 있는 형태로 저장
        StringRedisSerializer serializer = new StringRedisSerializer();
        template.setKeySerializer(serializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }

    /**
     * 채팅 Pub/Sub용 RedisTemplate (JSON 직렬화)
     *
     * ChatMessageResponse 객체를 JSON으로 직렬화하여 Redis 채널에 발행
     * StringRedisSerializer로는 객체 직렬화 불가 -> Jackson 기반 JSON 직렬화 사용
     */
    @Bean
    public RedisTemplate<String, Object> chatRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        /**
         * JavaTimeModule 등록한 ObjectMapper 설정
         * LocalDateTime 등 Java 8 날짜 타입 직렬화 지원
         */
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 타입 정보 포함 - 역직렬화 시 올바른 클래스로 복원
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        GenericJackson2JsonRedisSerializer serializer =
                new GenericJackson2JsonRedisSerializer(objectMapper);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(serializer);

        return template;
    }

    /**
     * Redis Pub/Sub 메시지 리스터 컨테이너
     *
     * PatternTopic("chat:room:*")
     * "chat:room:1", "chat:room:2" 등 모든 채팅방 채널을 단일 리스너로 처리
     */
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory redisConnectionFactory,
            ChatRedisSubscriber chatRedisSubscriber) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);

        // "chat:room:*" - 모든 채팅방 채널을 단일 리스너로 처리
        container.addMessageListener(chatRedisSubscriber,
                new PatternTopic("chat:room:*")
        );

        return container;
    }
}