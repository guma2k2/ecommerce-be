package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductVariantAttributeValueCreateRequest(
        @NotNull
        Long productAttributeId,
        @NotBlank
        String value
) {
}
