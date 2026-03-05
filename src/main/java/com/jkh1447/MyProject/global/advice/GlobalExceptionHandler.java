package com.jkh1447.MyProject.global.advice;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.jkh1447.MyProject.domain.auth.exception.AuthErrorCode;
import com.jkh1447.MyProject.domain.auth.exception.AuthException;
import com.jkh1447.MyProject.domain.auth.exception.RefreshTokenNotFoundFromCooKie;
import com.jkh1447.MyProject.domain.users.exception.UserNotFoundException;
import com.jkh1447.MyProject.global.exception.TooManyRequestException;
import com.jkh1447.MyProject.global.response.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.jkh1447.MyProject.domain.auth.exception.TokenException;
import com.jkh1447.MyProject.domain.auth.exception.TokenExpiredException;
import com.jkh1447.MyProject.domain.matching.exception.AlreadyInQueueException;
import com.jkh1447.MyProject.domain.matching.exception.InvalidStringFormatException;
import com.jkh1447.MyProject.domain.matching.exception.UserQueueInfoParsingException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(HttpStatus.BAD_REQUEST.name(), e.getMessage()));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiResponse<?>> handleUserException(AuthException e) {

        log.error("사용자 관련 오류 발생: {}", e.getMessage(), e);

        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(e.getHttpStatus().name(), e.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFoundException(UserNotFoundException e) {

        log.warn("사용자 없음 오류 발생: {}", e.getMessage(), e);

        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(e.getHttpStatus().name(), e.getMessage()));
    }

    // 만료토큰이 아닌 모든 유효하지 않은 토큰에 대해서 처리, 엑세스 토큰은 filter에서 처리하므로 이 핸들러는 리프래시 토큰을 위함임
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ApiResponse<?>> handleTokenException(TokenException e) {

        log.error("토큰 관련 오류 발생: {}", e.getMessage(), e);

        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(e.getHttpStatus().name(), e.getMessage()));
    }   

    // (리프래시)토큰이 만료되었을 경우
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponse<?>> handleTokenExpiredException(TokenExpiredException e) {

        log.error("리프래시 토큰 만료: {}", e.getMessage(), e);
        //프론트에서 로그인 페이지로 리다이렉트, 세션종료 알림
        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(AuthErrorCode.REFRESH_TOKEN_EXPIRED.name(), e.getMessage()));
    }

    @ExceptionHandler(RefreshTokenNotFoundFromCooKie.class)
    public ResponseEntity<ApiResponse<?>> handleRefreshTokenNotFoundFromCooKie(RefreshTokenNotFoundFromCooKie e) {

        log.error("리프래시 토큰 없음: {}", e.getMessage(), e);
        //프론트에서 로그인 페이지로 리다이렉트, 세션종료 알림
        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(AuthErrorCode.REFRESH_TOKEN_NOT_FOUND_FROM_COOKIE.name(), e.getMessage()));
    }

    // 추후 응답형식 수정
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllException(Exception e) {
        log.error("서버 내부 오류 발생: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다.");
    }

    @ExceptionHandler(InvalidStringFormatException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidStringFormatException(InvalidStringFormatException e) {

        log.error("잘못된 문자열 형식: {}", e.getMessage(), e);

        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(e.getHttpStatus().name(), ""));
    }

    @ExceptionHandler(UserQueueInfoParsingException.class)
    public ResponseEntity<ApiResponse<?>> handleUserQueueInfoParsingException(UserQueueInfoParsingException e) {

        log.error("유저 큐 정보 파싱 오류: {}", e.getMessage(), e);

        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(e.getHttpStatus().name(), ""));
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<ApiResponse<?>> handleTooManyRequestException(TooManyRequestException e) {

        log.error("너무 많은 요청: {}", e.getMessage(), e);

        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(e.getHttpStatus().name(), e.getMessage()));
    }

    @ExceptionHandler(AlreadyInQueueException.class)
    public ResponseEntity<ApiResponse<?>> handleAlreadyInQueueException(AlreadyInQueueException e) {

        log.error("이미 매칭 큐에 참가중: {}", e.getMessage(), e);

        return ResponseEntity.status(e.getHttpStatus()).body(ApiResponse.fail(e.getHttpStatus().name(), e.getMessage()));
    }
}
