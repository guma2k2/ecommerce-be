package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductTemplateCreateRequest(
        @NotBlank
        String name
) {
}
