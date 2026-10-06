package com.yas.system.inventory.internal.dto.response;

import com.yas.system.inventory.internal.entity.Inventory;

import java.time.ZonedDateTime;

public record InventoryResponse(
        Long id,
        Long productVariantId,
        String sku,
        int onHand,
        int reserved,
        int available,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static InventoryResponse from(Inventory entity) {
        if (entity == null) {
            return null;
        }
        return new InventoryResponse(
                entity.getId(),
                entity.getProductVariantId(),
                entity.getSku(),
                entity.getOnHand(),
                entity.getReserved(),
                entity.getAvailable(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
