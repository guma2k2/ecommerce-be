package com.yas.system.catalog.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductVariantOptionValueCreateRequest(
        @NotNull(message = ParamError.FIELD_NAME)
        Long productOptionId,
        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 100, message = ParamError.MAX_LENGTH)
        String value
) {
}
