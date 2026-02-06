package com.jkh1447.MyProject.dto.auth;

import com.jkh1447.MyProject.domain.auth.Gender;

public record UserSignupRequestDto(
    String loginId,
    String password,
    String email,
    String nickname,
    Gender gender
) {
}