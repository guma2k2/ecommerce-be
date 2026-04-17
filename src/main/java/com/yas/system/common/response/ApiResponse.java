package com.yas.system.common.response;

public record ApiResponse <T> (int status, String message, T data, String errorCode) {

    public static <T> ApiResponse<T> success (T data) {
        return new ApiResponse<>(200, "success", data, null);
    }

    public static <T> ApiResponse<T> error (int status, String message, String errorCode) {
        return new ApiResponse<>(status, message, null, errorCode);
    }
}
