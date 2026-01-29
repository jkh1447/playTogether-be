package com.jkh1447.MyProject.service.user;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import com.jkh1447.MyProject.repository.user.UserRepository;
import com.jkh1447.MyProject.repository.user.RefreshTokenRepository;
import com.jkh1447.MyProject.dto.user.UserLoginRequest;
import com.jkh1447.MyProject.dto.user.UserSignupRequest;
import com.jkh1447.MyProject.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.jkh1447.MyProject.domain.auth.RefreshToken;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.domain.auth.Users;
import com.jkh1447.MyProject.domain.auth.exception.DuplicateResourceException;
import com.jkh1447.MyProject.domain.auth.exception.LoginFailureException;
import com.jkh1447.MyProject.domain.auth.exception.TokenException;
import com.jkh1447.MyProject.domain.auth.exception.AuthErrorCode;
import com.jkh1447.MyProject.dto.user.Token;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(UserSignupRequest request) {

        if (userRepository.findByLoginId(request.loginId()).isPresent()) {
            throw new DuplicateResourceException(AuthErrorCode.USER_ID_DUPLICATE);
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException(AuthErrorCode.EMAIL_DUPLICATE);
        }

        if (userRepository.findByNickname(request.nickname()).isPresent()) {
            throw new DuplicateResourceException(AuthErrorCode.NICKNAME_DUPLICATE);
        }

        String encodedPassword = passwordEncoder.encode(request.password());


        Users user = Users.builder().loginId(request.loginId()).password(encodedPassword)
                .email(request.email()).nickname(request.nickname()).gender(request.gender())
                .build();

        userRepository.save(user);
    }

    public Token login(UserLoginRequest request) {
        Users user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new LoginFailureException(AuthErrorCode.USER_ID_OR_PASSWORD_NOT_MATCH));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new LoginFailureException(AuthErrorCode.USER_ID_OR_PASSWORD_NOT_MATCH);
        }

        String accessToken = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        saveRefreshToken(user.getId(), refreshToken);

        return new Token(accessToken, refreshToken);
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void saveRefreshToken(Long userId, String token) {
        // 기존에 이미 존재하는 리프레시 토큰이 있는지 확인
        refreshTokenRepository.findByUserId(userId).ifPresentOrElse(
                // 1. 이미 있다면 새로운 토큰으로 업데이트
                refreshToken -> {
                    refreshToken.updateToken(token);
                    refreshTokenRepository.save(refreshToken);
                },
                // 2. 없다면 새로 생성하여 저장
                () -> {
                    RefreshToken refreshToken =
                            RefreshToken.builder().userId(userId).refreshToken(token).build();
                    refreshTokenRepository.save(refreshToken);
                });
    }

    public String refreshAccessToken(String refreshToken) {

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new TokenException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken savedToken =
                refreshTokenRepository.findByRefreshToken(refreshToken).orElseThrow(
                        () -> new TokenException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        return jwtUtil.generateToken(savedToken.getUserId());

    }
}
