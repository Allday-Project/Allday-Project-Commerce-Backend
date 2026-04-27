package jpa.basic.alldayprojectcommerce.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jpa.basic.alldayprojectcommerce.domain.chat.redis.ChatRedisSubscriber;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jpa.basic.alldayprojectcommerce.common.cache.CacheName;
import jpa.basic.alldayprojectcommerce.common.cache.CacheType;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.stream.Collectors;

@Configuration
public class RedisConfig {

    private final RedisConnectionFactory cf;

    public RedisConfig(RedisConnectionFactory cf) {
        this.cf = cf;
    }

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


    // 데이터 직렬화 세팅(레디스 캐시의 기본 규칙 설정)
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return RedisCacheConfiguration.defaultCacheConfig() // 스프링 측 제공 레디스 기본 설정값 가져옴
                .serializeKeysWith(RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(new StringRedisSerializer())) // key를 저장할 때 자바 객체 아닌 일반 문자열(String)로 저장
                .serializeValuesWith(RedisSerializationContext
                        .SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper))) // value를 저장할 때 JSON 혈태로 변환해서 넣고, 꺼낼 때는 자바 객체로 역직렬화
                .disableCachingNullValues(); // DB 조회 결과가 null이면 레디스에 저장하지 않도록 막아줌
    }

    @Bean(name = "redisCacheManager") // 로컬 캐시와 구분하기 위해
    public CacheManager redisCacheManager(RedisCacheConfiguration cfg) {
        var configs = CacheName.entries().stream()
                .filter(g -> g.getCacheType() != CacheType.LOCAL) // 로컨 타입만 빼기
                .collect(Collectors.toMap(
                        CacheName::getCacheName,
                        g -> cfg.entryTtl(g.getTtl()) // enum에 선언해둔 각각의 만료 시간(TTL)을 덮어씌운 새로운 설정 객체로 지정
                ));

        // 레디스에 데이터를 쓸 때 동시성 락(Lock)을 걸지 않아서 성능을 높이는 방식으로 매니저 빌더를 시작
        // @Cacheable(sync = true) 로 어느 정도 커버(동시성 DB 조회를 막아줌)
        return RedisCacheManager.builder(RedisCacheWriter.nonLockingRedisCacheWriter(cf))
                .withInitialCacheConfigurations(configs)
                .build();
    }

}