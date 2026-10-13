package com.yas.system.payment.internal.gateway.dto;

import com.yas.system.payment.api.dto.CheckoutItemDto;

import java.util.List;

public record PaymentCheckoutContext(
        String customerEmail,
        List<CheckoutItemDto> items,
        String returnUrl,
        String cancelUrl
) {
}
