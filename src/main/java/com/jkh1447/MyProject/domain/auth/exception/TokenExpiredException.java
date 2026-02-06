package com.jkh1447.MyProject.domain.auth.exception;

public class TokenExpiredException extends AuthException{
    public TokenExpiredException() {
        super(AuthErrorCode.EXPIRED_TOKEN);
    }
    
}
