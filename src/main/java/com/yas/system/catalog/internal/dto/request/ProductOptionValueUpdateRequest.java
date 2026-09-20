package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductOptionValueUpdateRequest(
        Long id, // ProductOptionValue ID
        @NotBlank
        String value,
        int position
) {
}
