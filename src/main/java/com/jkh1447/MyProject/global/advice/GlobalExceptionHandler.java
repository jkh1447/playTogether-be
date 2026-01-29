package com.jkh1447.MyProject.global.advice;

import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.jkh1447.MyProject.domain.auth.exception.AuthException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import org.springframework.http.HttpStatus;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleUserException(AuthException e) {
        Map<String, String> response = new HashMap<>();
        response.put("code", e.getMessage()); 
        response.put("status", e.getHttpStatus().name()); 

        return ResponseEntity.status(e.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 내부 오류가 발생했습니다.");
    }
}
