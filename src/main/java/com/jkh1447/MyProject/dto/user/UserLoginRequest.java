package com.jkh1447.MyProject.dto.user;

public record UserLoginRequest(
    String loginId,
    String password
) {
}
