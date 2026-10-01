package com.yas.system.payment.api.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PaymentPublicDto(
        Long paymentId,
        String customerId,
        String orderId,
        BigDecimal amount,
        String currency,
        String status,
        String method,
        String stripeSessionId,
        String stripePaymentIntentId,
        ZonedDateTime createdAt
) {
}
