package com.yas.system.auth.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
        @NotBlank
        String email,
        @NotBlank
        String password,
        String console
) {}