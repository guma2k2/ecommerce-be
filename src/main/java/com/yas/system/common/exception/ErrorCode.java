package com.yas.system.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED("internal_error", "Uncategorized error"),
    UNAUTHENTICATED("unauthenticated", "Unauthenticated"),
    UNAUTHORIZED("unauthorized", "You don't have permission"),
    USER_NOT_FOUND("user_not_found", "User not found"),
    INCORRECT_PASSWORD("incorrect_password", "Incorrect password"),
    INVALID_TOKEN("invalid_token", "Invalid token"),
    INVALID_EMAIL("invalid_email", "Invalid email"),
    INVALID_CODE("invalid_code", "Invalid code"),
    INVALID_PROVIDER("invalid_provider", "Looks like you're signed up with %s account. Please use your %s account to login."),
    INVALID_PRODUCT("invalid_product", "Invalid product data"),
    PRODUCT_NOT_FOUND("product_not_found", "Product not found"),
    PRODUCT_ALREADY_EXISTS("product_already_exists", "Product already exists"),
    INVALID_CATEGORY("invalid_category", "Invalid category data"),
    CATEGORY_NOT_FOUND("category_not_found", "Category not found"),
    CATEGORY_ALREADY_EXISTS("category_already_exists", "Category already exists"),
    INVALID_BRAND("invalid_brand", "Invalid brand data"),
    BRAND_NOT_FOUND("brand_not_found", "Brand not found"),
    BRAND_ALREADY_EXISTS("brand_already_exists", "Brand already exists"),
    INVALID_PRODUCT_ATTRIBUTE("invalid_product_attribute", "Invalid product attribute data"),
    PRODUCT_ATTRIBUTE_NOT_FOUND("product_attribute_not_found", "Product attribute not found"),
    PRODUCT_ATTRIBUTE_ALREADY_EXISTS("product_attribute_already_exists", "Product attribute already exists"),
    INVALID_PRODUCT_TEMPLATE("invalid_product_template", "Invalid product template data"),
    PRODUCT_TEMPLATE_NOT_FOUND("product_template_not_found", "Product template not found"),
    PRODUCT_TEMPLATE_ALREADY_EXISTS("product_template_already_exists", "Product template already exists"),
    INVALID_MEDIA_TYPE("invalid_media_type", "Invalid media type: %s"),
    MEDIA_NOT_FOUND("media_not_found", "Media not found"),
    ROLE_NOT_FOUND("role_not_found", "Role not found"),
    ROLE_ALREADY_EXISTS("role_already_exists", "Role already exists"),
    ROLE_CANNOT_BE_DELETED("role_cannot_be_deleted", "Role cannot be deleted"),
    PERMISSION_NOT_FOUND("permission_not_found", "Permission not found"),
    ADMIN_PROFILE_NOT_FOUND("admin_profile_not_found", "Admin profile not found"),
    INVALID_PRODUCT_OPTION("invalid_product_option", "Invalid product option data"),
    PRODUCT_OPTION_NOT_FOUND("product_option_not_found", "Product option not found"),
    PRODUCT_OPTION_ALREADY_EXISTS("product_option_already_exists", "Product option already exists"),
    ;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String format(Object... args) {
        return String.format(message, args);
    }

    private final String code;
    private final String message;
}
