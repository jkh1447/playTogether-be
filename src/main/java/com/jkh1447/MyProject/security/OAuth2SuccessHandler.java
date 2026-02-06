package com.jkh1447.MyProject.security;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.domain.users.Users;
import com.jkh1447.MyProject.repository.user.UserRepository;
import com.jkh1447.MyProject.service.auth.AuthService;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpHeaders;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.jkh1447.MyProject.global.config.JWTConfig;
import com.jkh1447.MyProject.global.config.ServerConfig;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final JWTConfig jwtConfig;
    private final ServerConfig serverConfig;
    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;

        String email = extractEmail(oAuth2User, authToken.getAuthorizedClientRegistrationId());

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        authService.saveRefreshToken(user.getId().toString(), refreshToken);

        ResponseCookie accessTokenCookie =
                ResponseCookie.from(AuthConstants.TOKEN_NAME, accessToken).httpOnly(true)
                        .secure(jwtConfig.isCookieSecure()).path("/")
                        .maxAge(jwtConfig.getExpiration() / 1000)
                        .sameSite(jwtConfig.getCookieSameSite()).build();

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from(AuthConstants.REFRESH_TOKEN_NAME, refreshToken).httpOnly(true)
                        .secure(jwtConfig.isCookieSecure()).path("/")
                        .maxAge(jwtConfig.getRefreshExpiration() / 1000)
                        .sameSite(jwtConfig.getCookieSameSite()).build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        getRedirectStrategy().sendRedirect(request, response, serverConfig.getUrl());
    }

    private String extractEmail(OAuth2User oAuth2User, String registrationId) {
        // 네이버는 response 안에 정보가 있음
        if ("naver".equals(registrationId)) {
            Map<String, Object> response =
                    (Map<String, Object>) oAuth2User.getAttribute("response");
            if (response == null) {
                throw new IllegalArgumentException("Naver response attributes not found");
            }
            return (String) response.get("email");
        }


        return oAuth2User.getAttribute("email");
    }
}
