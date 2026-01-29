package com.jkh1447.MyProject.dto.user;

import com.jkh1447.MyProject.domain.auth.Gender;

public record UserSignupRequest(
    String loginId,
    String password,
    String email,
    String nickname,
    Gender gender
) {
}