package com.yas.system.payment.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedEvent(
        UUID paymentId,
        String customerId,
        String orderId,
        BigDecimal amount,
        String currency,
        String gatewayTransactionId
) {
}

