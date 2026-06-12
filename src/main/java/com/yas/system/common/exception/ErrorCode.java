package com.yas.system.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED("internal_error", "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED("unauthenticated", "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED("unauthorized", "You don't have permission", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND("user_not_found", "User not found", HttpStatus.NOT_FOUND),
    INCORRECT_PASSWORD("incorrect_password", "Incorrect password", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN("invalid_token", "Invalid token", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL("invalid_email", "Invalid email", HttpStatus.BAD_REQUEST),
    INVALID_CODE("invalid_code", "Invalid code", HttpStatus.BAD_REQUEST),
    INVALID_PROVIDER("invalid_provider", "Looks like you're signed up with %s account. Please use your %s account to login.", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(String code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    public String format(Object... args) {
        return String.format(message, args);
    }

    private final String code;
    private final String message;
    private final HttpStatusCode statusCode;


}
