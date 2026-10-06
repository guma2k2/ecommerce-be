package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Size;

public record RoleRequest (
        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 50, message = ParamError.MAX_LENGTH)
        String name,
        boolean isAllowListAll
) {
}
