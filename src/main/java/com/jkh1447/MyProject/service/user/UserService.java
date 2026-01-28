package com.jkh1447.MyProject.service.user;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import jakarta.transaction.Transactional;

import com.jkh1447.MyProject.repository.user.UserRepository;
import com.jkh1447.MyProject.domain.users.UserConstants;
import com.jkh1447.MyProject.domain.users.Users;
import com.jkh1447.MyProject.dto.user.UserLoginRequest;
import com.jkh1447.MyProject.dto.user.UserSignupRequest;
import com.jkh1447.MyProject.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(UserSignupRequest request) {

        String encodedPassword = passwordEncoder.encode(request.password());

        // 나중에 OAUTH를 위해서 비밀번호 null 검증하기
        Users user = Users.builder()
                .loginId(request.loginId())
                .password(encodedPassword)
                .email(request.email())
                .nickname(request.nickname())
                .build();

        userRepository.save(user);
    }

    public String login(UserLoginRequest request) {
        Users user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new IllegalArgumentException(UserConstants.LOGIN_ID_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException(UserConstants.PASSWORD_NOT_MATCH);
        }

        return jwtUtil.generateToken(user.getLoginId());
    }
}
