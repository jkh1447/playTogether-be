package com.jkh1447.MyProject.dto.auth;

public record TokenDto(
        String accessToken,
        String refreshToken) {
}