package com.yas.system.order.internal.dto.response;

import com.yas.system.order.internal.enumeration.OrderStatus;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderSummaryResponse(
        UUID id,
        String orderCode,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        int totalItems,
        String createdAt,
        String updatedAt
) {
}
