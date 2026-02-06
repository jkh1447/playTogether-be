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
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.domain.auth.exception.AuthErrorCode;
import com.jkh1447.MyProject.domain.auth.exception.NoAuthenticationInfoException;
import com.jkh1447.MyProject.domain.auth.exception.TokenException;
import com.jkh1447.MyProject.domain.auth.exception.TokenExpiredException;
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

    public String buildToken(String subject, long expiration) {
        return Jwts.builder().setSubject(subject).setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256).compact();
    }

    // userId는 db의 id임
    public String generateToken(Long userId) {
        return buildToken(userId.toString(), jwtConfig.getExpiration());
    }

    public String generateRefreshToken(Long userId) {
        return buildToken(userId.toString(), jwtConfig.getRefreshExpiration());
    }

    public String generateGuestToken(String userId) {
        String subject = AuthConstants.GUEST_TOKEN_PREFIX + userId;
        return buildToken(subject, jwtConfig.getExpiration());
    }

    public String generateGuestRefreshToken(String userId) {
        String subject = AuthConstants.GUEST_TOKEN_PREFIX + userId;
        return buildToken(subject, jwtConfig.getRefreshExpiration());
    }

    /**
     * Validates the given JWT token.
     * <p>
     * If the token is expired, throws an EXPIRED_TOKEN exception.
     * If the token is invalid, throws an INVALID_TOKEN exception.
     * </p>
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException();
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenException(AuthErrorCode.INVALID_TOKEN);
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

    // get user's id
    public static String getCurrentUserId() {
        final Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getName() == null) {
            throw new NoAuthenticationInfoException();
        }

        return authentication.getName();
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

    public String getSubjectFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody()
                .getSubject();
    }
}
