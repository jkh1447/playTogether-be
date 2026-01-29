package com.jkh1447.MyProject.dto.user;

public record Token(
        String accessToken,
        String refreshToken) {
}