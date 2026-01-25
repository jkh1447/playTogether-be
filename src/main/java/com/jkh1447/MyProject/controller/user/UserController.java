package com.jkh1447.MyProject.controller.user;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import lombok.RequiredArgsConstructor;
import com.jkh1447.MyProject.dto.user.UserSignupRequest;
import com.jkh1447.MyProject.service.user.UserService;
import com.jkh1447.MyProject.domain.users.UserConstants;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserSignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok(UserConstants.SIGNUP_SUCCESS);
    }


}
