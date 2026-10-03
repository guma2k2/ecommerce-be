package com.yas.system.payment.api.dto;

public record StripeVariantSyncResult(
        Long variantId,
        String stripeProductId,
        String stripePriceId
) {
}
