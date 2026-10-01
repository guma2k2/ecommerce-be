package com.yas.system.order.internal.dto.response;

import com.yas.system.order.internal.enumeration.OrderStatus;
import com.yas.system.payment.internal.enumeration.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreateResponse(
        UUID orderId,
        String orderCode,
        BigDecimal totalAmount,
        OrderStatus status,
        PaymentMethod paymentMethod,
        String checkoutUrl
) {
}
