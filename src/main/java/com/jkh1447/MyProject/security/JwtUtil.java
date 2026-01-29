package com.jkh1447.MyProject.security;

import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.Date;
import java.security.Key;
import jakarta.annotation.PostConstruct;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.global.config.JWTConfig;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JWTConfig jwtConfig;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
    }

    public String generateToken(Long userId) {
        return Jwts.builder().setSubject(userId.toString()).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder().setSubject(userId.toString()).setIssuedAt(new Date())
                .setExpiration(
                        new Date(System.currentTimeMillis() + jwtConfig.getRefreshExpiration()))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 예외처리 다음에 추가
            return false;
        }
    }

    /**
     * Authentication 객체는 다음과 같은 정보를 포함한다. 1. Principal : 사용자 정보 2. Credentials
     * : 사용자 비밀번호 3. Authorities : 사용자 권한
     */
    public Authentication getAuthentication(String token) {
        // claim은 jwt토큰의 payload 부분에 해당하는 데이터들을 의미함.
        Claims claims =
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        // UserDetails란 스프링 시큐리티가 사용자의 정보를 담는 인터페이스.
        UserDetails userDetails = new User(claims.getSubject(), "", new ArrayList<>());

        // 최종 신분증인 Authentication을 발급
        return new UsernamePasswordAuthenticationToken(userDetails, "",
                userDetails.getAuthorities());
    }

    public static Long getCurrentUserId() {
        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No authentication information");
        }

        return Long.parseLong(authentication.getName());
    }

    public static String getTokenFromCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
