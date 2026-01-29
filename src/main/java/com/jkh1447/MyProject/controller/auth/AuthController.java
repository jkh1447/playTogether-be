package com.jkh1447.MyProject.controller.auth;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.jkh1447.MyProject.dto.user.UserSignupRequest;
import com.jkh1447.MyProject.global.config.JWTConfig;
import com.jkh1447.MyProject.security.JwtUtil;
import com.jkh1447.MyProject.dto.user.UserLoginRequest;
import com.jkh1447.MyProject.service.user.AuthService;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.dto.user.Token;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService userService;
	private final JWTConfig jwtConfig;

	@PostMapping("/signup")
	public ResponseEntity<Map<String, String>> signup(@RequestBody UserSignupRequest request) {
		userService.signup(request);
		Map<String, String> response = new HashMap<>();
		response.put("message", AuthConstants.SIGNUP_SUCCESS);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {

		Token tokenDto = userService.login(request);

		// 쿠키 설정
		ResponseCookie accessTokenCookie = ResponseCookie.from(AuthConstants.TOKEN_NAME, tokenDto.accessToken())
				.httpOnly(true) // 자바스크립트 접근 차단 (보안 핵심)
				.secure(jwtConfig.isCookieSecure()) // HTTPS가 아닐 때도 허용 (로컬 테스트용, 배포시 true)
				.path("/") // 모든 경로에서 쿠키 사용
				.maxAge(jwtConfig.getExpiration() / 1000) // 쿠키 유효 시간 (1시간)
				.sameSite(jwtConfig.getCookieSameSite()) // CSRF 공격 방지
				.build();

		ResponseCookie refreshTokenCookie = ResponseCookie.from(AuthConstants.REFRESH_TOKEN_NAME, tokenDto.refreshToken())
				.httpOnly(true)
				.secure(jwtConfig.isCookieSecure()) 
				.path("/") 
				.maxAge(jwtConfig.getRefreshExpiration() / 1000) 
				.sameSite(jwtConfig.getCookieSameSite()) 
				.build();

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
				.header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
				.build();

	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletRequest request) {

		Long userId = JwtUtil.getCurrentUserId();

		userService.logout(userId);

		ResponseCookie cookie = ResponseCookie.from(AuthConstants.TOKEN_NAME, "")
				.httpOnly(true)
				.secure(jwtConfig.isCookieSecure())
				.path("/")
				.maxAge(0)
				.sameSite(jwtConfig.getCookieSameSite())
				.build();

		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.build();

	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(HttpServletRequest request) {
		String refreshToken = JwtUtil.getTokenFromCookie(request, AuthConstants.REFRESH_TOKEN_NAME);
		if (refreshToken == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("리프레시 토큰이 없습니다.");
		}

		String newAccessToken = userService.refreshAccessToken(refreshToken);
		log.info("새로운 액세스 토큰이 발급되었습니다.");
		ResponseCookie cookie = ResponseCookie.from(AuthConstants.TOKEN_NAME, newAccessToken)
				.httpOnly(true)
				.secure(jwtConfig.isCookieSecure())
				.path("/")
				.maxAge(jwtConfig.getExpiration() / 1000)
				.sameSite(jwtConfig.getCookieSameSite())
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
