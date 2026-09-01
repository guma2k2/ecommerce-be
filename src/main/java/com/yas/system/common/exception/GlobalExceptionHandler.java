package com.yas.system.common.exception;

import com.yas.system.common.response.APIStatus;
import com.yas.system.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex){
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.ok(ApiResponse.error(APIStatus.ERR_BAD_REQUEST.getCode() + "", errorMessage));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.ok(ApiResponse.error(ex.getErrorCode().getCode(), ex.getErrorCode().getMessage()));
    }

    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidData(InvalidDataException ex) {
        return ResponseEntity.ok(ApiResponse.error(ex.getErrorCode().getCode(), ex.getErrorCode().getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.ok(ApiResponse.error(APIStatus.ERR_BAD_REQUEST.getCode() + "", "Email or password is incorrect"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(Exception ex){
        return ResponseEntity.ok(ApiResponse.error(ErrorCode.UNCATEGORIZED.getCode(), ErrorCode.UNCATEGORIZED.getMessage()));
    }
}
