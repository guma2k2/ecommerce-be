package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Email(message = ParamError.INVALID_EMAIL)
        String email,

        @NotBlank
        String password

//        String console
) {}