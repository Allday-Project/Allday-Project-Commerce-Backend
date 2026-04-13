package jpa.basic.alldayprojectcommerce.common.security.config;

import jpa.basic.alldayprojectcommerce.common.security.JwtAuthenticationFilter;
import jpa.basic.alldayprojectcommerce.common.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(options -> options.sameOrigin()))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/", "/login", "/signup",
                        "/cart", "/mypage", "/orders", "/orders/**",
                        "/css/**", "/js/**", "/images/**",
                        "/assets/**", "/img/**", "/error", "/favicon.ico",
                        "/h2-console/**"
                ).permitAll()
                .requestMatchers(HttpMethod.POST,
                        "/api/auth/login",
                        "/api/auth/signup",
                        "/api/auth/reissue",
                        "/api/orders"
                ).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/check-duplicate").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                    new JwtAuthenticationFilter(jwtTokenProvider),
                    UsernamePasswordAuthenticationFilter.class
            );
        return http.build();
    }
}