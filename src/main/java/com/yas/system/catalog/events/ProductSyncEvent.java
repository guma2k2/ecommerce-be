package com.yas.system.catalog.events;

public record ProductSyncEvent(
        Long productId,
        SyncAction action
) {
}
