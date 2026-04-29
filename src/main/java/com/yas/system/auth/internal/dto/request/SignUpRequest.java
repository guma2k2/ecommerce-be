package com.yas.system.auth.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank
        String email,

        @NotBlank
        String password,
        String firstName,
        String lastName
) {}