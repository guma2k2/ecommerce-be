package com.yas.system.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED("internal_error", "Uncategorized error"),
    UNAUTHENTICATED("unauthenticated", "Unauthenticated"),
    UNAUTHORIZED("unauthorized", "You don't have permission"),
    BAD_REQUEST("bad_request", "Bad request"),
    USER_NOT_FOUND("user_not_found", "User not found"),
    INCORRECT_PASSWORD("incorrect_password", "Incorrect password"),
    INVALID_TOKEN("invalid_token", "Invalid token"),
    INVALID_EMAIL("invalid_email", "Invalid email"),
    INVALID_CODE("invalid_code", "Invalid code"),
    INVALID_PROVIDER("invalid_provider", "Looks like you're signed up with %s account. Please use your %s account to login."),
    INVALID_PRODUCT("invalid_product", "Invalid product data"),
    PRODUCT_NOT_FOUND("product_not_found", "Product not found"),
    PRODUCT_ALREADY_EXISTS("product_already_exists", "Product already exists"),
    PRODUCT_NAME_ALREADY_EXISTS("product_name_already_exists", "Product name already exists"),
    PRODUCT_SLUG_ALREADY_EXISTS("product_slug_already_exists", "Product slug already exists"),
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
    CUSTOMER_PROFILE_NOT_FOUND("customer_profile_not_found", "Customer profile not found"),
    INVALID_PRODUCT_OPTION("invalid_product_option", "Invalid product option data"),
    PRODUCT_OPTION_NOT_FOUND("product_option_not_found", "Product option not found"),
    PRODUCT_OPTION_ALREADY_EXISTS("product_option_already_exists", "Product option already exists"),
    FILE_TOO_LARGE("file_too_large", "File size exceeds maximum allowed limit: %s"),
    MAX_UPLOAD_SIZE_EXCEEDED("max_upload_size_exceeded", "Maximum upload size exceeded"),
    CART_ITEM_NOT_FOUND("cart_item_not_found", "Cart item not found"),
    PRODUCT_VARIANT_NOT_FOUND("product_variant_not_found", "Product variant not found"),
    INSUFFICIENT_STOCK("insufficient_stock", "Requested quantity (%s) exceeds available stock (%s)"),
    PRODUCT_VARIANT_INACTIVE("product_variant_inactive", "Product variant is unavailable"),
    PAYMENT_NOT_FOUND("payment_not_found", "Payment with ID %s not found"),
    STRIPE_CHECKOUT_FAILED("stripe_checkout_failed", "Failed to create Stripe checkout session: %s"),
    INVALID_WEBHOOK_SIGNATURE("invalid_webhook_signature", "Invalid Stripe webhook signature"),
    PAYMENT_ALREADY_PROCESSED("payment_already_processed", "Payment has already been processed"),
    ORDER_NOT_FOUND("order_not_found", "Order with ID %s not found"),
    ORDER_ITEMS_EMPTY("order_items_empty", "Order must contain at least one item"),
    ORDER_CANNOT_BE_CANCELLED("order_cannot_be_cancelled", "Order %s cannot be cancelled in status: %s"),
    INVENTORY_NOT_FOUND("inventory_not_found", "Inventory for product variant %s not found"),
    INVALID_STOCK_ADJUSTMENT("invalid_stock_adjustment", "Stock adjustment failed: %s"),
    RESERVATION_NOT_FOUND("reservation_not_found", "Stock reservation for order %s not found"),
    RESERVATION_ALREADY_PROCESSED("reservation_already_processed", "Reservation for order %s has already been processed"),
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
