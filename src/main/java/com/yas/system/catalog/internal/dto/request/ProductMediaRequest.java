package com.yas.system.catalog.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductMediaRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 36, message = ParamError.MAX_LENGTH)
        String mediaId,

        Integer position
) {
}
