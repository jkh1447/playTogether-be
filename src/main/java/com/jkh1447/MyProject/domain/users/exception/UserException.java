package com.jkh1447.MyProject.domain.users.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class UserException extends RuntimeException {
    private final HttpStatus httpStatus;

    public UserException(UserErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getHttpStatus();
    }
}
