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
    INVALID_PRODUCT("invalid_product", "Invalid product data", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND("product_not_found", "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_ALREADY_EXISTS("product_already_exists", "Product already exists", HttpStatus.BAD_REQUEST),
    INVALID_CATEGORY("invalid_category", "Invalid category data", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND("category_not_found", "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_ALREADY_EXISTS("category_already_exists", "Category already exists", HttpStatus.BAD_REQUEST),
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
