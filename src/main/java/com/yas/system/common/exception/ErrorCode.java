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
    INVALID_BRAND("invalid_brand", "Invalid brand data", HttpStatus.BAD_REQUEST),
    BRAND_NOT_FOUND("brand_not_found", "Brand not found", HttpStatus.NOT_FOUND),
    BRAND_ALREADY_EXISTS("brand_already_exists", "Brand already exists", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_ATTRIBUTE("invalid_product_attribute", "Invalid product attribute data", HttpStatus.BAD_REQUEST),
    PRODUCT_ATTRIBUTE_NOT_FOUND("product_attribute_not_found", "Product attribute not found", HttpStatus.NOT_FOUND),
    PRODUCT_ATTRIBUTE_ALREADY_EXISTS("product_attribute_already_exists", "Product attribute already exists", HttpStatus.BAD_REQUEST),
    INVALID_PRODUCT_TEMPLATE("invalid_product_template", "Invalid product template data", HttpStatus.BAD_REQUEST),
    PRODUCT_TEMPLATE_NOT_FOUND("product_template_not_found", "Product template not found", HttpStatus.NOT_FOUND),
    PRODUCT_TEMPLATE_ALREADY_EXISTS("product_template_already_exists", "Product template already exists", HttpStatus.BAD_REQUEST),
    INVALID_MEDIA_TYPE("invalid_media_type", "Invalid media type: %s", HttpStatus.BAD_REQUEST),
    MEDIA_NOT_FOUND("media_not_found", "Media not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND("role_not_found", "Role not found", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS("role_already_exists", "Role already exists", HttpStatus.BAD_REQUEST),
    ROLE_CANNOT_BE_DELETED("role_cannot_be_deleted", "Role cannot be deleted", HttpStatus.BAD_REQUEST),
    PERMISSION_NOT_FOUND("permission_not_found", "Permission not found", HttpStatus.NOT_FOUND),
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
