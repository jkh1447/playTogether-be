package com.jkh1447.MyProject.domain.auth.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class AuthException extends RuntimeException {
    private final HttpStatus httpStatus;

    public AuthException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }
}
