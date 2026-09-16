package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record ProductVariantCreateRequest(
        String title,
        @NotBlank
        String sku,
        @NotNull
        @PositiveOrZero
        BigDecimal price,
        @PositiveOrZero
        int quantity,
        String mediaId,
        @Valid
        List<ProductVariantAttributeValueCreateRequest> attributeValues
) {
}
