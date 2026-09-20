package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record ProductVariantUpdateRequest(
        Long id,
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
        List<ProductVariantOptionValueUpdateRequest> optionValues,
        @Valid
        List<ProductVariantAttributeValueUpdateRequest> attributeValues
) {
    public ProductVariantUpdateRequest(
            Long id,
            String title,
            String sku,
            BigDecimal price,
            int quantity,
            String mediaId,
            List<ProductVariantAttributeValueUpdateRequest> attributeValues
    ) {
        this(id, title, sku, price, quantity, mediaId, null, attributeValues);
    }
}
