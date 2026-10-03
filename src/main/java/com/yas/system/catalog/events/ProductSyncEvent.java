package com.yas.system.catalog.events;

import java.util.List;

public record ProductSyncEvent(
        Long productId,
        SyncAction action,
        List<VariantStripeInfo> removedVariants
) {
    public ProductSyncEvent(Long productId, SyncAction action) {
        this(productId, action, List.of());
    }
}
