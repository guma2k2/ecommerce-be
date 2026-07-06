package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductTemplateRequest(
        @NotBlank
        String name
) {
}
