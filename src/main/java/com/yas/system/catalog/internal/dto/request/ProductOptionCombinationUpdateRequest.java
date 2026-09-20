package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductOptionCombinationUpdateRequest(
        @NotNull
        Long productOptionId,
        int position,
        @Valid
        List<ProductOptionValueUpdateRequest> values
) {
}
