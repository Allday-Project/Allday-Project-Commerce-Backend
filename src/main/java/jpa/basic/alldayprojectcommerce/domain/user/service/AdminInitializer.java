package jpa.basic.alldayprojectcommerce.domain.user.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer {
    
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    @Transactional
    public void initAdmin() {
        String email = "admin@allday.com";
        String encodedPassword = passwordEncoder.encode("admin1234");
        
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM users WHERE email = ?", Integer.class, email);
                
            if (count == null || count == 0) {
                jdbcTemplate.update(
                    "INSERT INTO users (email, password, name, phone, address, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())",
                    email, encodedPassword, "관리자", "010-0000-0000", "관리자 기본 주소", "ADMIN"
                );
                log.info("관리자 계정이 성공적으로 생성되었습니다: {}", email);
            }
        } catch (Exception e) {
            log.error("관리자 계정 생성 중 오류 발생: {}", e.getMessage());
        }
    }
}
