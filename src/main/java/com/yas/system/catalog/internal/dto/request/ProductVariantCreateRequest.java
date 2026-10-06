package com.yas.system.catalog.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ProductVariantCreateRequest(
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
        List<ProductVariantOptionValueCreateRequest> optionValues,

        @Valid
        List<ProductVariantAttributeValueCreateRequest> attributeValues
) {
    public ProductVariantCreateRequest(
            String title,
            String sku,
            BigDecimal price,
            int quantity,
            String mediaId,
            List<ProductVariantAttributeValueCreateRequest> attributeValues
    ) {
        this(title, sku, price, quantity, mediaId, null, attributeValues);
    }
}
