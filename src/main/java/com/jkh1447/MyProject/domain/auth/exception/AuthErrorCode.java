package com.jkh1447.MyProject.domain.auth.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum AuthErrorCode {

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN"),

    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_EXPIRED"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND"),
    REFRESH_TOKEN_NOT_FOUND_FROM_COOKIE(HttpStatus.UNAUTHORIZED, "REFRESH_TOKEN_NOT_FOUND_FROM_COOKIE"),

    NO_AUTHENTICATION_INFO(HttpStatus.UNAUTHORIZED, "NO_AUTHENTICATION_INFO"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND");

    private final HttpStatus httpStatus;
    private final String message;

    AuthErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
