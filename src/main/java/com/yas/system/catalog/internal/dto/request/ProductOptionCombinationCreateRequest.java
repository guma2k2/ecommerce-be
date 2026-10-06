package com.yas.system.catalog.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductOptionCombinationCreateRequest(
        @NotNull(message = ParamError.FIELD_NAME)
        Long productOptionId,
        int position,
        @Valid
        List<ProductOptionValueCreateRequest> values
) {
}
