package com.jkh1447.MyProject.domain.matching.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum MatchingErrorCode {

    INVALID_PARTICIPANT_STRING_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "매칭 참가자 문자열 형식이 올바르지 않습니다."),
    INVALID_QUEUE_KEY_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "매칭 큐 키 형식이 올바르지 않습니다."),
    INVALID_QUEUE_USER_INFO_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "매칭 큐 유저 정보 형식이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    MatchingErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
