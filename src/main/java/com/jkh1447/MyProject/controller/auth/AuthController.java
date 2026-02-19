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
import com.jkh1447.MyProject.global.config.JWTConfig;
import com.jkh1447.MyProject.global.response.ApiResponse;
import com.jkh1447.MyProject.security.JwtUtil;
import com.jkh1447.MyProject.service.auth.AuthService;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.domain.auth.exception.RefreshTokenNotFoundFromCooKie;
import com.jkh1447.MyProject.dto.auth.TokenDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService userService;
	private final JWTConfig jwtConfig;
	private final JwtUtil jwtUtil;

	// @PostMapping("/signup")
	// public ResponseEntity<Map<String, String>> signup(@RequestBody UserSignupRequestDto request)
	// {
	// userService.signup(request);
	// Map<String, String> response = new HashMap<>();
	// response.put("message", AuthConstants.SIGNUP_SUCCESS);
	// return ResponseEntity.ok(response);
	// }

	// @PostMapping("/login")
	// public ResponseEntity<?> login(@RequestBody UserLoginRequestDto request) {

	// TokenDto tokenDto = userService.login(request);

	// // 쿠키 설정
	// ResponseCookie accessTokenCookie =
	// ResponseCookie.from(AuthConstants.TOKEN_NAME, tokenDto.accessToken()).httpOnly(true)
	// .secure(jwtConfig.isCookieSecure())
	// .path("/")
	// .maxAge(jwtConfig.getExpiration() / 1000)
	// .sameSite(jwtConfig.getCookieSameSite())
	// .build();

	// ResponseCookie refreshTokenCookie =
	// ResponseCookie.from(AuthConstants.REFRESH_TOKEN_NAME, tokenDto.refreshToken())
	// .httpOnly(true).secure(jwtConfig.isCookieSecure()).path("/")
	// .maxAge(jwtConfig.getRefreshExpiration() / 1000)
	// .sameSite(jwtConfig.getCookieSameSite()).build();

	// return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
	// .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString()).build();

	// }

	@PostMapping("/guest")
	public ResponseEntity<?> issueGuestToken(HttpServletResponse response) {
		TokenDto tokenDto = userService.createGuestToken();
		ResponseCookie accessTokenCookie =
				ResponseCookie.from(AuthConstants.TOKEN_NAME, tokenDto.accessToken()).httpOnly(true)
						.secure(jwtConfig.isCookieSecure()).path("/")
						.maxAge(jwtConfig.getExpiration() / 1000)
						.sameSite(jwtConfig.getCookieSameSite()).build();

		ResponseCookie refreshTokenCookie =
				ResponseCookie.from(AuthConstants.REFRESH_TOKEN_NAME, tokenDto.refreshToken())
						.httpOnly(true).secure(jwtConfig.isCookieSecure()).path("/")
						.maxAge(jwtConfig.getRefreshExpiration() / 1000)
						.sameSite(jwtConfig.getCookieSameSite()).build();

		response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

		return ResponseEntity.ok()
				.body(ApiResponse.success(AuthConstants.GUEST_TOKEN_ISSUED));
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(HttpServletResponse response) {

		try {
			String userId = JwtUtil.getCurrentUserId();
			userService.logout(userId);
		} catch (Exception e) {
			/*
			 * 로그아웃 요청했지만 토큰이 이미 만료되었을 경우 오류를 잡기만하고 토큰을 제거하는 로직은 계속 진행. 
			 *  RestControllerAdvice에서 처리하기 까다로움.
			 */

			log.info("로그아웃 요청했지만 토큰이 이미 만료되었습니다.");
		}

		ResponseCookie accessTokenCookie = ResponseCookie.from(AuthConstants.TOKEN_NAME, "").httpOnly(true)
				.secure(jwtConfig.isCookieSecure()).path("/").maxAge(0)
				.sameSite(jwtConfig.getCookieSameSite()).build();

		ResponseCookie refreshTokenCookie =
				ResponseCookie.from(AuthConstants.REFRESH_TOKEN_NAME, "").httpOnly(true)
						.secure(jwtConfig.isCookieSecure()).path("/").maxAge(0) // 즉시 만료
						.sameSite(jwtConfig.getCookieSameSite()).build();

		log.info("로그아웃 되었습니다.");

		response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

		return ResponseEntity.ok().body(ApiResponse.success(AuthConstants.LOGOUT_SUCCESS));
	}

	@PostMapping("/refresh")
	public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {

		String refreshToken = JwtUtil.getTokenFromCookie(request, AuthConstants.REFRESH_TOKEN_NAME);
		if (refreshToken == null) {
			throw new RefreshTokenNotFoundFromCooKie();
		}
		// refresh token도 다시 새로 발급

		TokenDto tokenDto = userService.refreshAccessToken(refreshToken); 
		String newAccessToken = tokenDto.accessToken();
		String newRefreshToken = tokenDto.refreshToken();
		
		log.info("새로운 액세스 토큰이 발급되었습니다.");


		ResponseCookie accessTokenCookie = ResponseCookie.from(AuthConstants.TOKEN_NAME, newAccessToken)
				.httpOnly(true).secure(jwtConfig.isCookieSecure()).path("/")
				.maxAge(jwtConfig.getExpiration() / 1000).sameSite(jwtConfig.getCookieSameSite())
				.build();

		ResponseCookie refreshTokenCookie = ResponseCookie.from(AuthConstants.REFRESH_TOKEN_NAME, newRefreshToken)
				.httpOnly(true).secure(jwtConfig.isCookieSecure()).path("/")
				.maxAge(jwtConfig.getRefreshExpiration() / 1000).sameSite(jwtConfig.getCookieSameSite())
				.build();

		response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
		response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
		
		return ResponseEntity.ok().build();
	}

	@GetMapping("/test/me")
	public ResponseEntity<String> testAuth() {
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !auth.isAuthenticated()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("신분증이 없네요!");
		}

		return ResponseEntity.ok("인증 성공! 당신의 아이디는: " + auth.getName());
	}
}
