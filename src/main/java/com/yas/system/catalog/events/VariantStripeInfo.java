package com.yas.system.catalog.events;

public record VariantStripeInfo(
        Long variantId,
        String stripeProductId,
        String stripePriceId
) {
}
