package com.yas.system.common.exception;

import com.yas.system.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex){
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .findFirst()
                .map(this::resolveErrorMessage)
                .orElse(ErrorCode.BAD_REQUEST.getMessage());
        log.error("Validation error: {}", errorMessage);
        return ResponseEntity.ok(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), errorMessage));
    }

    String resolveErrorMessage(ObjectError error) {
        String message = error.getDefaultMessage();
        if (message == null || message.isBlank()) {
            return (error instanceof FieldError fe ? fe.getField() : "Field") + " is invalid";
        }

        if (error instanceof FieldError fe) {
            String fieldName = fe.getField();
            if (message.contains("{fieldName}")) {
                message = message.replace("{fieldName}", fieldName);
            }
            if (message.contains("${validatedValue")) {
                String nullOrEmpty = (fe.getRejectedValue() == null) ? "null" : "empty";
                message = message.replaceAll("\\$\\{validatedValue[^}]*\\}", nullOrEmpty);
            }
        }

        return message;
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse> handleException(ApplicationException ex) {
        return ResponseEntity.ok(ApiResponse.error(ex.getErrorCode().getCode(), ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(BadCredentialsException ex) {
        log.error("Error: {}", ex.getMessage());
        return ResponseEntity.ok(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), "Email or password is incorrect"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.ok(ApiResponse.error(
                ErrorCode.MAX_UPLOAD_SIZE_EXCEEDED.getCode(),
                ErrorCode.MAX_UPLOAD_SIZE_EXCEEDED.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleRuntimeException(Exception ex){
        log.error("Unhandled exception: ", ex);
        return ResponseEntity.ok(ApiResponse.error(ErrorCode.UNCATEGORIZED.getCode(), ErrorCode.UNCATEGORIZED.getMessage()));
    }
}
