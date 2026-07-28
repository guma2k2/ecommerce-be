package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BrandCreateRequest(
        @NotBlank
        String name,
        String description
) {
}
