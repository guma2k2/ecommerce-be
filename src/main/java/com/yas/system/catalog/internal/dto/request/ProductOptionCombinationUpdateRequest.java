package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotNull;

public record ProductOptionCombinationUpdateRequest(
        @NotNull
        Long productOptionId,
        int position
) {
}
