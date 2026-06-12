package com.yas.system.common.response;

public record ApiResponse <T> (String status, String message, T data) {

    public static <T> ApiResponse<T> success (T data) {
        return new ApiResponse<>("200", "success", data);
    }

    public static <T> ApiResponse<T> successWithNoContent () {
        return new ApiResponse<>("204", "success", null);
    }

    public static <T> ApiResponse<T> error (String status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
