package com.jkh1447.MyProject.security;

import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.context.SecurityContextHolder;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        if(request.getCookies() != null) {
            for(Cookie cookie : request.getCookies()) {
                if("accessToken".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if(token != null && jwtUtil.validateToken(token)) {
            var authentication = jwtUtil.getAuthentication(token);

            // 현재 요청 보관함에 신분증을 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        //
        filterChain.doFilter(request, response);
    }
}
