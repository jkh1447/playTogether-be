package com.jkh1447.MyProject.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jkh1447.MyProject.domain.auth.exception.AuthErrorCode;
import com.jkh1447.MyProject.domain.auth.exception.TokenExpiredException;
import com.jkh1447.MyProject.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.context.SecurityContextHolder;
import com.jkh1447.MyProject.domain.auth.AuthConstants;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 엑세스 토큰 만료시 여기서 잡아서 401던지기
        try {
            String token = JwtUtil.getTokenFromCookie(request, AuthConstants.TOKEN_NAME);
            // System.out.println("보낸 토큰 확인: " + token);
            if (token != null && jwtUtil.validateToken(token)) {
                var authentication = jwtUtil.getAuthentication(token);

                // 현재 요청 보관함에 신분증을 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (TokenExpiredException e) {
            sendErrorResponse(response, AuthErrorCode.EXPIRED_TOKEN);
            return;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, AuthErrorCode.INVALID_TOKEN);
            return;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, AuthErrorCode errorCode)
            throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.fail(errorCode.name(), errorCode.getMessage());

        String json = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(json);
    }
}
