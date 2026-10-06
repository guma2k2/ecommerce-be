package com.yas.system.catalog.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.Size;

public record ProductVariantOptionValueUpdateRequest(
        Long productOptionValueId,
        Long productOptionId,
        @Size(max = 100, message = ParamError.MAX_LENGTH)
        String value
) {
}
