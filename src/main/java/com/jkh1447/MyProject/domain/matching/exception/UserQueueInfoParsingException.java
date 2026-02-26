package com.jkh1447.MyProject.domain.matching.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class UserQueueInfoParsingException extends RuntimeException {
    HttpStatus httpStatus;

    public UserQueueInfoParsingException(MatchingErrorCode errorCode) {
        super(errorCode.getMessage());
        this.httpStatus = errorCode.getHttpStatus();
    }
}
