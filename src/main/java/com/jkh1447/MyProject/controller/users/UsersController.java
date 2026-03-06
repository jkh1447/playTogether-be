package com.jkh1447.MyProject.controller.users;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.jkh1447.MyProject.service.users.UsersService;
import jakarta.servlet.http.HttpServletResponse;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.domain.auth.Role;
import com.jkh1447.MyProject.domain.users.Users;
import com.jkh1447.MyProject.domain.users.exception.UserNotFoundException;
import com.jkh1447.MyProject.dto.users.UserInfoResponse;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import com.jkh1447.MyProject.global.config.JWTConfig;
import com.jkh1447.MyProject.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final UsersService usersService;
    private final JWTConfig jwtConfig;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String name = authentication.getName();

        log.info("name: {}", name);
        if (name.startsWith(AuthConstants.GUEST_TOKEN_PREFIX)) { // 게스트인 경우
            String guestIdPart = name.split("_")[1].substring(0, 4);
            UserInfoResponse response = UserInfoResponse.builder().role(Role.ROLE_GUEST)
                    .nickname(AuthConstants.GUEST_NICKNAME_PREFIX + guestIdPart)
                    .userId(authentication.getName()).email("guest").build();
            return ResponseEntity.ok(ApiResponse.success(response));
        }
        Long userId = Long.parseLong(name);

        UserInfoResponse response = usersService.getMyInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @DeleteMapping("/me")
    public ResponseEntity<?> withdraw(Authentication authentication, HttpServletResponse response) {

        usersService.withdraw(authentication);

        ResponseCookie accessTokenCookie = ResponseCookie.from(AuthConstants.TOKEN_NAME, "")
                .httpOnly(true).secure(jwtConfig.isCookieSecure()).path("/").maxAge(0)
                .sameSite(jwtConfig.getCookieSameSite()).build();

        ResponseCookie refreshTokenCookie =
                ResponseCookie.from(AuthConstants.REFRESH_TOKEN_NAME, "").httpOnly(true)
                        .secure(jwtConfig.isCookieSecure()).path("/").maxAge(0) // 즉시 만료
                        .sameSite(jwtConfig.getCookieSameSite()).build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
