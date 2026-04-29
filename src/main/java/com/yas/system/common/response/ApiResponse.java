package com.yas.system.common.response;

public record ApiResponse <T> (int status, String message, T data) {

    public static <T> ApiResponse<T> success (T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> error (int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
