package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ProductRequest(
        @NotBlank
        String name,
        String description,
        @NotBlank
        String slug,
        @Valid
        @NotEmpty
        ProductVariantRequest[] variants,
        @Valid
        @NotEmpty
        ProductOptionRequest[] options
) {
}
