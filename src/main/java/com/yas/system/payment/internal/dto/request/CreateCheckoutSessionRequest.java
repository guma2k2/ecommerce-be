package com.yas.system.payment.internal.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateCheckoutSessionRequest(
        String orderId,
        @NotNull @DecimalMin(value = "0.50", message = "Amount must be at least 0.50")
        BigDecimal amount,
        @NotBlank
        String currency
) {
}
