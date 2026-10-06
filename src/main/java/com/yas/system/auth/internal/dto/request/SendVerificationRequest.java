package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendVerificationRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Email(message = ParamError.INVALID_EMAIL)
        @Size(max = 254, message = ParamError.MAX_LENGTH)
        String email
) {
}
