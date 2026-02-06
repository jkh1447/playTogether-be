package com.jkh1447.MyProject.domain.auth.exception;

public class NoAuthenticationInfoException extends AuthException{
    public NoAuthenticationInfoException() {
        super(AuthErrorCode.NO_AUTHENTICATION_INFO);
    }
    
}
