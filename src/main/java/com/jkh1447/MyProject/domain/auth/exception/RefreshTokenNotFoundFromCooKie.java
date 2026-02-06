package com.jkh1447.MyProject.domain.auth.exception;

public class RefreshTokenNotFoundFromCooKie extends AuthException {
    public RefreshTokenNotFoundFromCooKie() {
        super(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND_FROM_COOKIE);
    }
}
