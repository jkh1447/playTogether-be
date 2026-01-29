package com.jkh1447.MyProject.domain.auth.exception;

public class LoginFailureException extends AuthException {
    public LoginFailureException(AuthErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode.getHttpStatus());
    }
}
