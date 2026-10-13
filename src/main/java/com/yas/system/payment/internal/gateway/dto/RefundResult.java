package com.yas.system.payment.internal.gateway.dto;

import java.math.BigDecimal;

public record RefundResult(
        boolean successful,
        String gatewayRefundId,
        BigDecimal refundedAmount,
        String failureReason
) {
}
