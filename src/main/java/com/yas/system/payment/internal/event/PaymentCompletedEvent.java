package com.yas.system.payment.internal.event;

import java.math.BigDecimal;

public record PaymentCompletedEvent(
        Long paymentId,
        String customerId,
        String orderId,
        BigDecimal amount,
        String currency,
        String stripePaymentIntentId
) {
}
