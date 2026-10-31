package com.yas.system.payment.internal.dto.response;

import com.yas.system.common.util.DateTimeUtils;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.enumeration.PaymentMethod;
import com.yas.system.payment.enumeration.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        String customerId,
        String orderId,
        BigDecimal amount,
        BigDecimal refundedAmount,
        String currency,
        PaymentStatus status,
        PaymentMethod method,
        String gatewaySessionId,
        String gatewayTransactionId,
        String failureReason,
        String createdAt,
        String updatedAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getCustomerId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getRefundedAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getGatewaySessionId(),
                payment.getGatewayTransactionId(),
                payment.getFailureReason(),
                DateTimeUtils.format(payment.getCreatedAt()),
                DateTimeUtils.format(payment.getUpdatedAt())
        );
    }
}
