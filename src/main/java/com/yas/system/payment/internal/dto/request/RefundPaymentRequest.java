package com.yas.system.payment.internal.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RefundPaymentRequest(
        @NotNull
        @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
        BigDecimal amount,

        @Size(max = 255)
        String reason
) {
}
