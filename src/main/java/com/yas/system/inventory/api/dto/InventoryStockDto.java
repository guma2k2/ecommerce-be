package com.yas.system.inventory.api.dto;

public record InventoryStockDto(
        Long productVariantId,
        String sku,
        int onHand,
        int reserved,
        int available
) {
}
