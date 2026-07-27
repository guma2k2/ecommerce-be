package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductRequest(
        @NotBlank
        String name,
        String description,
        @NotBlank
        String slug,
        String metaTitle,
        String metaKeyword,
        String metaDescription,
        Integer categoryId,
        Long brandId,

        @Valid
        List<ProductAttributeValueRequest> attributes,

        @Valid
        @NotEmpty
        List<ProductVariantRequest> variants,

        @Valid
        @NotNull
        List<ProductOptionRequest> options
) {
}
