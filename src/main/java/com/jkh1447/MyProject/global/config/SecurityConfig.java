package com.jkh1447.MyProject.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.jkh1447.MyProject.security.JwtAuthenticationFilter;
import com.jkh1447.MyProject.security.JwtUtil;
import com.jkh1447.MyProject.security.OAuth2SuccessHandler;
import com.jkh1447.MyProject.service.auth.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ServerConfig serverConfig;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final ObjectMapper objectMapper;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 시큐리티 차원의 CORS 허용 설정
                .cors(cors -> cors.configurationSource(request -> {
                    var config = new org.springframework.web.cors.CorsConfiguration();
                    config.setAllowCredentials(true);
                    config.setAllowedOrigins(java.util.List.of(
                            serverConfig.getUrl(),
                            "http://pt-alb-573380929.ap-northeast-2.elb.amazonaws.com",
                            "https://pt-alb-573380929.ap-northeast-2.elb.amazonaws.com"
                    ));
                    config.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    config.setAllowedHeaders(java.util.List.of("*"));
                    return config;
                }))
                // 2. CSRF 비활성화 (Rest API 방식은 세션을 쓰지 않으므로 끕니다)
                .csrf(csrf -> csrf.disable())
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, objectMapper), UsernamePasswordAuthenticationFilter.class)
                // 3. 경로별 권한 설정
                .exceptionHandling(exception -> exception
                        // 인증되지 않은 사용자가 접근했을 때 401을 반환하도록 설정
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/report", "/api/chatMessageLog", "/api/report/update/status").hasRole("ADMIN")
                        .requestMatchers("/api/feedback", "/api/feedback/update/status").hasRole("ADMIN")
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/guest", "/api/auth/logout", "/api/auth/refresh", "/api/users/me").permitAll() // 회원가입, 로그인
                        .requestMatchers("/oauth2/**", "/login/oauth2/**", "/api/gameInfo/**", "/api/feedback/category/**").permitAll()
                        .anyRequest().authenticated() // 나머지는 토큰이 있어야 함
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(requestCache -> requestCache.disable());

        return http.build();
    }
}
