package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Email(message = ParamError.INVALID_EMAIL)
        String email,

        @NotBlank(message = ParamError.FIELD_NAME)
        String password,

        @NotBlank(message = ParamError.FIELD_NAME)
        String name
) {}