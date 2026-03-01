package com.jkh1447.MyProject.domain.matching.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class AlreadyInQueueException extends RuntimeException {
    HttpStatus httpStatus;

    public AlreadyInQueueException() {
        super(MatchingErrorCode.ALREADY_IN_QUEUE.getMessage());
        this.httpStatus = MatchingErrorCode.ALREADY_IN_QUEUE.getHttpStatus();
    }
}
