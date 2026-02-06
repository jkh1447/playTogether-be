package com.jkh1447.MyProject.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message));
    }

    @Getter
    @AllArgsConstructor
    static class ErrorResponse {
        private String code;
        private String message;
    }
}