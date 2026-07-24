package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;

public record AdminProfileRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        String name,
        String avatar
) {
}
