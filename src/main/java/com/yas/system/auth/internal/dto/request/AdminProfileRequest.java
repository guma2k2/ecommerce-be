package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminProfileRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 100, message = ParamError.MAX_LENGTH)
        String name,

        @Size(max = 1024, message = ParamError.MAX_LENGTH)
        String avatar
) {
}
