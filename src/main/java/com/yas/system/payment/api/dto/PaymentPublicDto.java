package com.yas.system.payment.api.dto;

import com.yas.system.payment.internal.entity.Payment;

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

    public static PaymentPublicDto from(Payment payment) {
        return new PaymentPublicDto(
                payment.getId(),
                payment.getCustomerId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getMethod().name(),
                payment.getStripeSessionId(),
                payment.getStripePaymentIntentId(),
                payment.getCreatedAt()
        );
    }
}
