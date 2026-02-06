package com.jkh1447.MyProject.domain.users.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND");

    private final HttpStatus httpStatus;
    private final String message;

    UserErrorCode(HttpStatus httpStatus, String message) {
        this.message = message;
        this.httpStatus = httpStatus;
    }

}
