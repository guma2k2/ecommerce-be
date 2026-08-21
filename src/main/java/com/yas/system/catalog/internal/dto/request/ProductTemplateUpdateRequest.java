package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProductTemplateUpdateRequest(
        @NotBlank
        String name,
        List<Long> attributeIds
) {
}
