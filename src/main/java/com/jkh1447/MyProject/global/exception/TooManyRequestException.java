package com.jkh1447.MyProject.global.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class TooManyRequestException extends RuntimeException {
    HttpStatus httpStatus;

    public TooManyRequestException() {
        super(GlobalErrorCode.TOO_MANY_REQUESTS.getMessage());
        this.httpStatus = GlobalErrorCode.TOO_MANY_REQUESTS.getHttpStatus();
    }
}
