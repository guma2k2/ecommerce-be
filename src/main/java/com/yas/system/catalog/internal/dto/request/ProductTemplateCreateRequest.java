package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record ProductTemplateCreateRequest(
        @NotBlank
        String name,
        List<Long> attributeIds
) {
}
