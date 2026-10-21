package com.yas.system.promotion.internal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record VerifyPromotionRequest(
        @NotBlank(message = "Coupon code is required")
        String couponCode,

        @NotNull(message = "Order amount is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Order amount must be greater than zero")
        BigDecimal orderAmount,

        @NotEmpty(message = "Items list cannot be empty")
        List<@Valid VerifyPromotionItemRequest> items
) {
}
