package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BrandUpdateRequest(
        @NotBlank
        String name,
        String description
) {
}
