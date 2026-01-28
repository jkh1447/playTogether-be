package com.jkh1447.MyProject.controller.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.dto.user.UserSignupRequest;
import com.jkh1447.MyProject.dto.user.UserLoginRequest;
import com.jkh1447.MyProject.service.user.UserService;
import com.jkh1447.MyProject.domain.users.UserConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody UserSignupRequest request) {
        userService.signup(request);
        Map<String, String> response = new HashMap<>();
        response.put("message", UserConstants.SIGNUP_SUCCESS);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {

        String token = userService.login(request);

        // 쿠키 설정
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true) // 자바스크립트 접근 차단 (보안 핵심)
                .secure(false) // HTTPS가 아닐 때도 허용 (로컬 테스트용, 배포시 true)
                .path("/") // 모든 경로에서 쿠키 사용
                .maxAge(60 * 60) // 쿠키 유효 시간 (1시간)
                .sameSite("Lax") // CSRF 공격 방지
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();

    }

    @GetMapping("/test/me")
    public ResponseEntity<String> testAuth() {
        // 필터가 신분증(Authentication)을 잘 넣어줬다면 여기서 꺼낼 수 있습니다.
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("신분증이 없네요!");
        }

        return ResponseEntity.ok("인증 성공! 당신의 아이디는: " + auth.getName());
    }
}
