package com.yas.system.promotion.internal.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VerifyPromotionItemRequest(
        @NotNull(message = "Product variant ID is required")
        Long productVariantId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @NotNull(message = "Unit price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be greater than zero")
        BigDecimal unitPrice
) {
}
