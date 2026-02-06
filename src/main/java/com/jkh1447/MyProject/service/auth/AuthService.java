package com.jkh1447.MyProject.service.auth;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import com.jkh1447.MyProject.repository.auth.RefreshTokenRepository;
import com.jkh1447.MyProject.repository.user.UserRepository;
import com.jkh1447.MyProject.security.JwtUtil;
import java.util.UUID;
import com.jkh1447.MyProject.domain.auth.RefreshToken;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.domain.auth.exception.TokenException;
import com.jkh1447.MyProject.domain.users.Users;
import com.jkh1447.MyProject.dto.auth.TokenDto;
import com.jkh1447.MyProject.dto.auth.UserLoginRequestDto;
import com.jkh1447.MyProject.dto.auth.UserSignupRequestDto;
import com.jkh1447.MyProject.domain.auth.exception.AuthErrorCode;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final JwtUtil jwtUtil;

    // @Transactional
    // public void signup(UserSignupRequestDto request) {

    // if (userRepository.findByLoginId(request.loginId()).isPresent()) {
    // throw new DuplicateResourceException(AuthErrorCode.USER_ID_DUPLICATE);
    // }

    // if (userRepository.findByEmail(request.email()).isPresent()) {
    // throw new DuplicateResourceException(AuthErrorCode.EMAIL_DUPLICATE);
    // }

    // if (userRepository.findByNickname(request.nickname()).isPresent()) {
    // throw new DuplicateResourceException(AuthErrorCode.NICKNAME_DUPLICATE);
    // }

    // String encodedPassword = passwordEncoder.encode(request.password());

    // Users user =
    // Users.builder().loginId(request.loginId()).password(encodedPassword)
    // .email(request.email()).nickname(request.nickname()).gender(request.gender())
    // .build();

    // userRepository.save(user);
    // }

    // public TokenDto login(UserLoginRequestDto request) {
    // Users user = userRepository.findByLoginId(request.loginId()).orElseThrow(
    // () -> new
    // LoginFailureException(AuthErrorCode.USER_ID_OR_PASSWORD_NOT_MATCH));

    // if (!passwordEncoder.matches(request.password(), user.getPassword())) {
    // throw new LoginFailureException(AuthErrorCode.USER_ID_OR_PASSWORD_NOT_MATCH);
    // }

    // String accessToken = jwtUtil.generateToken(user.getId());
    // String refreshToken = jwtUtil.generateRefreshToken(user.getId());

    // saveRefreshToken(user.getId(), refreshToken);

    // return new TokenDto(accessToken, refreshToken);
    // }

    @Transactional
    public void logout(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public void saveRefreshToken(String userId, String token) {

        int updatedRows = refreshTokenRepository.updateRefreshToken(userId, token);
        if (updatedRows == 0) {
            try {
                refreshTokenRepository
                        .save(RefreshToken.builder().userId(userId).refreshToken(token).build());
            } catch (DataIntegrityViolationException e) {
                // 그 찰나에 다른 요청이 먼저 Insert 했다면 다시 한번 Update
                refreshTokenRepository.updateRefreshToken(userId, token);
            }
        }
    }

    /**
     * Validates the refresh token and issues a new access token.
     * <p>
     * guest: Issued after validation only
     * <p>
     * user: Issued after validation and verification against the database
     */
    @Transactional
    public TokenDto refreshAccessToken(String refreshToken) {

        jwtUtil.validateToken(refreshToken);

        // guest 토큰이면 guest 토큰으로 발급
        if (jwtUtil.getSubjectFromToken(refreshToken)
                .startsWith(AuthConstants.GUEST_TOKEN_PREFIX)) {
            String subject = jwtUtil.getSubjectFromToken(refreshToken)
                    .replace(AuthConstants.GUEST_TOKEN_PREFIX, "");
            String guestAccessToken = jwtUtil.generateGuestToken(subject);
            String guestRefreshToken = jwtUtil.generateGuestRefreshToken(subject);
            saveRefreshToken(subject, guestRefreshToken);
            return new TokenDto(guestAccessToken, guestRefreshToken);
        }

        refreshTokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new TokenException(AuthErrorCode.INVALID_TOKEN));

        Long userId = Long.valueOf(jwtUtil.getSubjectFromToken(refreshToken));
        String userAccessToken = jwtUtil.generateToken(userId);
        String userRefreshToken = jwtUtil.generateRefreshToken(userId);
        saveRefreshToken(userId.toString(), userRefreshToken);

        return new TokenDto(userAccessToken, userRefreshToken);
    }

    @Transactional
    public TokenDto createGuestToken() {
        String guestUuid = UUID.randomUUID().toString();

        String accessToken = jwtUtil.generateGuestToken(guestUuid);
        String refreshToken = jwtUtil.generateGuestRefreshToken(guestUuid);

        saveRefreshToken(guestUuid, refreshToken);

        return new TokenDto(accessToken, refreshToken);
    }

}
