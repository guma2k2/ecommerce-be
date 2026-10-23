package com.yas.system.payment.internal.gateway.dto;

import com.yas.system.payment.api.dto.CheckoutItemDto;

import java.util.List;

public record PaymentCheckoutContext(
        String customerEmail,
        List<CheckoutItemDto> items,
        String returnUrl,
        String cancelUrl,
        String stripePromotionCodeId,
        String stripeCouponId
) {
    public PaymentCheckoutContext(String customerEmail, List<CheckoutItemDto> items, String returnUrl, String cancelUrl) {
        this(customerEmail, items, returnUrl, cancelUrl, null, null);
    }
}
