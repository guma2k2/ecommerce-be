package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
        @Email(message = ParamError.INVALID_EMAIL)
        String email,
        String console
) {
}
