package com.yas.system.order.event;

import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID orderId,
        String orderCode,
        String customerId,
        String customerEmail,
        BigDecimal totalAmount,
        String currency,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus
) {
}
