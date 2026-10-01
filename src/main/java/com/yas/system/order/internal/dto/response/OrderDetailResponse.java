package com.yas.system.order.internal.dto.response;

import com.yas.system.order.internal.enumeration.OrderStatus;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public record OrderDetailResponse(
        UUID id,
        String orderCode,
        OrderStatus status,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        BigDecimal shippingFee,
        ShippingAddressResponse shippingAddress,
        String note,
        ZonedDateTime createdAt,
        List<OrderItemResponse> items
) {
}
