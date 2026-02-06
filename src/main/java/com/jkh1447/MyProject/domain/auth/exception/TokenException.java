package com.jkh1447.MyProject.domain.auth.exception;

public class TokenException extends AuthException {
    public TokenException(AuthErrorCode errorCode) {
        super(errorCode);
    }
}
