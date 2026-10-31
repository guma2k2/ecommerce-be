package com.yas.system.payment.event;

import com.yas.system.payment.enumeration.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundCompletedEvent(
        UUID paymentId,
        String orderId,
        String customerId,
        String customerEmail,
        BigDecimal refundedAmount,
        BigDecimal totalAmount,
        String currency,
        PaymentStatus newPaymentStatus,
        String reason
) {
}

