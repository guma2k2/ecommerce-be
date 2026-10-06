package com.yas.system.inventory.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockReservationItemDto(
        @NotNull
        Long productVariantId,

        @Min(1)
        int quantity
) {
}
