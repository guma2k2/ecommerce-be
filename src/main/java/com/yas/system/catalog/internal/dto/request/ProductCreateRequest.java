package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductCreateRequest(
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
        List<ProductOptionCombinationCreateRequest> options,

        @Valid
        List<ProductAttributeValueCreateRequest> attributes,

        @Valid
        @NotEmpty
        List<ProductVariantCreateRequest> variants
) {
}
