package com.jkh1447.MyProject.dto.auth;

public record UserLoginRequestDto(
    String loginId,
    String password
) {
}
