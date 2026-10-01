package com.yas.system.payment.internal.dto.response;

import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record PaymentResponse(
        Long id,
        String customerId,
        String orderId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        PaymentMethod method,
        String stripeSessionId,
        String stripePaymentIntentId,
        String failureReason,
        ZonedDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getCustomerId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getStripeSessionId(),
                payment.getStripePaymentIntentId(),
                payment.getFailureReason(),
                payment.getCreatedAt()
        );
    }
}
