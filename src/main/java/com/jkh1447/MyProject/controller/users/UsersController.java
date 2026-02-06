package com.jkh1447.MyProject.controller.users;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.jkh1447.MyProject.service.users.UsersService;
import com.jkh1447.MyProject.domain.auth.AuthConstants;
import com.jkh1447.MyProject.domain.auth.Role;
import com.jkh1447.MyProject.domain.users.Users;
import com.jkh1447.MyProject.dto.users.UserResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import com.jkh1447.MyProject.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UsersController {

    private final UsersService usersService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String name = authentication.getName();
        
        log.info("name: {}", name);
        if(name.startsWith(AuthConstants.GUEST_TOKEN_PREFIX)) { // 게스트인 경우
            UserResponse response = UserResponse.builder()
                    .role(Role.GUEST)
                    .nickname("")
                    .userId(authentication.getName())
                    .build();
            return ResponseEntity.ok(ApiResponse.success(response));
        }
        Long userId = Long.parseLong(name);
        
        UserResponse response = usersService.getMyInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
