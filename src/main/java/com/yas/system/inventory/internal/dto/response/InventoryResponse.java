package com.yas.system.inventory.internal.dto.response;

import com.yas.system.common.util.DateTimeUtils;
import com.yas.system.inventory.internal.entity.Inventory;

public record InventoryResponse(
        Long id,
        Long productVariantId,
        String sku,
        int onHand,
        int reserved,
        int available,
        String createdAt,
        String updatedAt
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
                DateTimeUtils.format(entity.getCreatedAt()),
                DateTimeUtils.format(entity.getUpdatedAt())
        );
    }
}
