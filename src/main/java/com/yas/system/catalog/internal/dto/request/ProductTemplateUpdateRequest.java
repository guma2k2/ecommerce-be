package com.yas.system.catalog.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductTemplateUpdateRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 100, message = ParamError.MAX_LENGTH)
        String name,
        List<Long> attributeIds
) {
}
