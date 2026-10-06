package com.yas.system.auth.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UserRequest(
        @NotBlank(message = ParamError.FIELD_NAME)
        @Email(message = ParamError.INVALID_EMAIL)
        @Size(max = 254, message = ParamError.MAX_LENGTH)
        String email,

        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(min = 8, max = 60, message = ParamError.MAX_LENGTH)
        String password,

        @NotBlank(message = ParamError.FIELD_NAME)
        @Size(max = 100, message = ParamError.MAX_LENGTH)
        String name,

        @NotEmpty(message = ParamError.FIELD_NAME)
        List<Integer> roleIds
) {
}
