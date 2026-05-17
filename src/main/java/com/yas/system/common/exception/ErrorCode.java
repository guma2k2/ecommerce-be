package com.yas.system.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "You don't have permission", HttpStatus.FORBIDDEN),

    // Not found : 1003 - 1500
    USER_NOT_FOUND(1003, "User not found", HttpStatus.NOT_FOUND),


    // Invalid data: conflict ,existed : 1501 - 2000
    INCORRECT_PASSWORD(1501, "Incorrect password", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1502, "Invalid token", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1503, "Invalid email", HttpStatus.BAD_REQUEST),
    INVALID_CODE(1504, "Invalid code", HttpStatus.BAD_REQUEST),

    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;


}
