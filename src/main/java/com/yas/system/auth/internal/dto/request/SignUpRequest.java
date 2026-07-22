package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.enumeration.Language;
import com.yas.system.common.response.ParamError;
import com.yas.system.common.validation.annotation.ValidateEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SignUpRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Email(message = ParamError.INVALID_EMAIL)
        String email,

        @NotBlank(message = ParamError.FIELD_NAME)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
                message = "Password must have at least 1 lowercase letter, 1 uppercase letter, 1 number, 1 special character and minimum 8 characters"
        )
        String password,

        @NotBlank(message = ParamError.FIELD_NAME)
        String name,

        @NotBlank(message = ParamError.FIELD_NAME)
        @ValidateEnum(enumClass = Language.class, message = "Language must be one of: EN, VI")
        String language
) {}