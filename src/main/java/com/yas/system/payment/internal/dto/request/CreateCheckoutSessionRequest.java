package com.yas.system.payment.internal.dto.request;

import com.yas.system.common.response.ParamError;
import com.yas.system.common.validation.annotation.ValidateEnum;
import com.yas.system.payment.internal.constant.PaymentConstant;
import com.yas.system.payment.enumeration.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateCheckoutSessionRequest(
        @Size(max = 36)
        String orderId,

        @NotNull(message = ParamError.FIELD_NAME)
        @DecimalMin(value = PaymentConstant.MIN_CHECKOUT_AMOUNT, message = "Amount must be at least " + PaymentConstant.MIN_CHECKOUT_AMOUNT)
        BigDecimal amount,

        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 3)
        String currency,

        @NotBlank(message = ParamError.FIELD_NAME)
        @ValidateEnum(enumClass = PaymentMethod.class, message = "Invalid payment method")
        String paymentMethod,

        String returnUrl,

        String cancelUrl
) {
}
