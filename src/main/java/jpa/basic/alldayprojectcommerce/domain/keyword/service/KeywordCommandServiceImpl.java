package jpa.basic.alldayprojectcommerce.domain.keyword.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class KeywordCommandServiceImpl implements KeywordCommandService {

    private final RedisTemplate<String, String> redisTemplate;

    // 한글, 영문, 숫자, 공백만 허용 (특수문자 전부 제거)
    private static final Pattern SPECIAL_CHAR = Pattern.compile("[^가-힣a-zA-Z0-9\\s]");

    /**
     * Redis 키 형식: "search:rank:<현재 날짜>"
     * 스케쥴러가 특정 날짜 키만 골라서 삭제할 수 있도록 설계
     */
    private String todayRankKey() {
        return "search:rank:" + LocalDate.now()
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * 검색어 기록
     *
     * 1. 특수문자 제거 + 공백 기준 단어 파싱
     * 2. 각 단어를 Redis ZSet에 ZINCRBY로 +1
     */
    @Override
    public void createRecordSearch(String query) {
        // 빈 검색어 무시
        if (query == null || query.isBlank()) {
            return;
        }

        // 특수문자 제거 후 앞뒤 공백 제거
        String cleaned = SPECIAL_CHAR.matcher(query.trim()).replaceAll("");

        // 공백 기준으로 단어 분리
        String[] keywords = cleaned.split("\\s+");

        String key = todayRankKey();

        for (String keyword : keywords) {
            // 1글자 이하는 의미없는 검색어로 간주하고 제외
            if (keyword.isBlank() || keyword.length() < 2) {
                continue;
            }

            // 키워드가 처음이면 score = 1 새로 생성
            // 이미 있으면 기존 score에 1을 더함
            redisTemplate.opsForZSet().incrementScore(key, keyword, 1);

            log.debug("[검색어 기록] keyword: {}, key: {}", keyword, key);
        }
    }
}
