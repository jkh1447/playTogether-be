package com.jkh1447.MyProject.domain.auth.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum AuthErrorCode {
    
    USER_ID_DUPLICATE(HttpStatus.CONFLICT, "USER_ID_DUPLICATE"),
    EMAIL_DUPLICATE(HttpStatus.CONFLICT, "EMAIL_DUPLICATE"),
    NICKNAME_DUPLICATE(HttpStatus.CONFLICT, "NICKNAME_DUPLICATE"),
    USER_ID_OR_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "USER_ID_OR_PASSWORD_NOT_MATCH"),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND");
    

    private final HttpStatus httpStatus;
    private final String message;

    AuthErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
