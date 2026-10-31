package com.yas.system.payment.internal.gateway.dto;

import com.yas.system.payment.enumeration.PaymentStatus;

public record WebhookResult(
        boolean successful,
        String gatewayEventId,
        String gatewayTransactionId,
        String orderId,
        PaymentStatus paymentStatus,
        String failureReason,
        String rawPayload
) {
}
