package com.yas.system.order.internal.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String orderCode,
        String customerId,
        BigDecimal totalAmount,
        List<Long> variantIds
) {
}
