package com.yas.system.common.exception;

import com.yas.system.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidation(MethodArgumentNotValidException ex){
        String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        log.error("Error: {}", errorMessage);
        return ResponseEntity.ok(ApiResponse.error(ErrorCode.BAD_REQUEST.getCode(), ErrorCode.BAD_REQUEST.getMessage()));
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
