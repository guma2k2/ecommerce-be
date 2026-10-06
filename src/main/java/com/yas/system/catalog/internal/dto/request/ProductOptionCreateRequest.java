package com.yas.system.catalog.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductOptionCreateRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 50, message = ParamError.MAX_LENGTH)
        String name
) {
}
