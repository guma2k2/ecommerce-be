package com.yas.system.payment.internal.gateway.dto;

import com.yas.system.payment.enumeration.PaymentStatus;

import java.math.BigDecimal;

public record PaymentStatusResult(
        PaymentStatus status,
        String gatewayTransactionId,
        BigDecimal amount,
        String currency,
        String failureReason,
        String rawStatus
) {
}
