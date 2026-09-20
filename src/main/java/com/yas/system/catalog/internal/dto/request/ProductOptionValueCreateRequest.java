package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductOptionValueCreateRequest(
        @NotBlank
        String value,
        int position
) {
}
