package com.mango.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.StringUtils;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(AdminAccountProperties.class)
public class SecurityConfig {

    /**
     * 콘텐츠를 변경하거나 비용이 발생하는 경로.
     *
     * content-generator는 /admin/ 경로가 아니지만 OpenAI를 호출해
     * 비용이 나가므로 함께 보호한다.
     */
    private static final String[] PROTECTED_PATHS = {
            "/api/v1/admin/**",
            "/api/v1/content-generator/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PROTECTED_PATHS).authenticated()
                        .anyRequest().permitAll()
                )
                /*
                 * SPA가 직접 401을 받아 로그인 화면을 띄우도록,
                 * 브라우저 기본 인증 팝업을 유발하는 WWW-Authenticate 헤더는 보내지 않는다.
                 */
                .httpBasic(basic -> basic.authenticationEntryPoint(
                        (request, response, exception) ->
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED)
                ))
                /*
                 * 세션을 두지 않고 요청마다 인증하므로 CSRF 토큰이 필요 없다.
                 */
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * 관리자는 한 명이고 사용자 테이블이 없어 메모리에 둔다.
     *
     * 설정이 없으면 기동을 실패시킨다. 기본 비밀번호를 두면
     * 환경 변수를 잊은 채 배포됐을 때 그것이 그대로 백도어가 된다.
     */
    @Bean
    public UserDetailsService adminUserDetailsService(
            AdminAccountProperties properties,
            Environment environment
    ) {
        String username = properties.getUsername();
        String password = properties.getPassword();

        if (!StringUtils.hasText(username)
                || !StringUtils.hasText(password)) {
            throw new IllegalStateException(
                    "관리자 계정이 설정되지 않았습니다. "
                            + "ADMIN_USERNAME과 ADMIN_PASSWORD 환경 변수를 설정하세요."
            );
        }

        if (password.startsWith("{noop}")
                && environment.matchesProfiles("prod")) {
            throw new IllegalStateException(
                    "운영 프로파일에서는 평문({noop}) 비밀번호를 쓸 수 없습니다. "
                            + "{bcrypt} 해시를 사용하세요."
            );
        }

        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(password)
                        .roles("ADMIN")
                        .build()
        );
    }
}
