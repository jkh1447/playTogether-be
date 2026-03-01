package com.jkh1447.MyProject.global.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum GlobalErrorCode {

    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "너무 많은 요청을 보냈습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    GlobalErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
