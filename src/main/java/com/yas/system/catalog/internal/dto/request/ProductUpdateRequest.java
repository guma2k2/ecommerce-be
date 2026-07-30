package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductUpdateRequest(
        @NotBlank
        String name,
        String description,
        @NotBlank
        String slug,
        String metaTitle,
        String metaKeyword,
        String metaDescription,
        Integer categoryId,
        Integer brandId,
        @Valid
        List<ProductOptionCombinationUpdateRequest> options,

        @Valid
        List<ProductAttributeValueUpdateRequest> attributes,

        @Valid
        @NotEmpty
        List<ProductVariantUpdateRequest> variants
) {
}
