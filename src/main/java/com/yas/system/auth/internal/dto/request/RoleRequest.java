package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;

public record RoleRequest (
        @NotBlank(message = ParamError.FIELD_NAME)
        String name,
        boolean isAllowListAll
) {
}
