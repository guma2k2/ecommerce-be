package com.yas.system.payment.internal.dto.request;

import com.yas.system.payment.internal.constant.PaymentConstant;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCheckoutSessionRequest(
        @Size(max = 36)
        String orderId,
        @NotNull @DecimalMin(value = PaymentConstant.MIN_CHECKOUT_AMOUNT, message = "Amount must be at least " + PaymentConstant.MIN_CHECKOUT_AMOUNT)
        BigDecimal amount,
        @NotBlank
        @Size(max = 3)
        String currency
) {
}
