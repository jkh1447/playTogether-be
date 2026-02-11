package com.jkh1447.MyProject.domain.matching.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class InvalidStringFormatException extends RuntimeException {
    HttpStatus httpStatus;

    public InvalidStringFormatException(MatchingErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getHttpStatus();
    }
}
