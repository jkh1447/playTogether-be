package com.jkh1447.MyProject.domain.auth.exception;
    
public class DuplicateResourceException extends AuthException {
    public DuplicateResourceException(AuthErrorCode errorCode) {
        super(errorCode.getMessage(), errorCode.getHttpStatus());
    }
}
