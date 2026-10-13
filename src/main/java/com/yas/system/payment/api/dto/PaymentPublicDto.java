package com.yas.system.payment.api.dto;

import com.yas.system.common.util.DateTimeUtils;
import com.yas.system.payment.internal.entity.Payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentPublicDto(
        UUID paymentId,
        String customerId,
        String orderId,
        BigDecimal amount,
        BigDecimal refundedAmount,
        String currency,
        String status,
        String method,
        String gatewaySessionId,
        String gatewayTransactionId,
        String createdAt
) {

    public static PaymentPublicDto from(Payment payment) {
        return new PaymentPublicDto(
                payment.getId(),
                payment.getCustomerId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getRefundedAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getMethod().name(),
                payment.getGatewaySessionId(),
                payment.getGatewayTransactionId(),
                DateTimeUtils.format(payment.getCreatedAt())
        );
    }
}
