package com.yas.system.catalog.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductAttributeValueCreateRequest(
        @NotNull(message = ParamError.FIELD_NAME)
        Long productAttributeId,
        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 500, message = ParamError.MAX_LENGTH)
        String value
) {
}
