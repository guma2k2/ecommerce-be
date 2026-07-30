package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductOptionCombinationCreateRequest(
        @NotNull
        Long productOptionId,
        int position,
        List<ProductOptionValueCreateRequest> values
) {
}
