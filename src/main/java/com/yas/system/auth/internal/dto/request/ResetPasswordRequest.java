package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        String token,

        @NotBlank(message = ParamError.FIELD_NAME)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Password must have at least 1 lowercase letter, 1 uppercase letter, 1 number, 1 special character and minimum 8 characters"
        )
        String password
) {
}
