package com.jkh1447.MyProject.dto.user;

public record UserSignupRequest(
    String loginId,
    String password,
    String email,
    String nickname
) {
}