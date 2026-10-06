package com.yas.system.catalog.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductVariantUpdateRequest(
        Long id,

        @Size(max = 150, message = ParamError.MAX_LENGTH)
        String title,

        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 64, message = ParamError.MAX_LENGTH)
        String sku,

        @NotNull
        @PositiveOrZero
        BigDecimal price,

        @PositiveOrZero
        int quantity,

        @Size(max = 36, message = ParamError.MAX_LENGTH)
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
