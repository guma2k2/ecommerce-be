package com.yas.system.auth.internal.dto;

import jakarta.validation.constraints.NotBlank;

public class AuthDto {
    public record SignInRequest(
            @NotBlank
            String email,
            @NotBlank
            String password,
            String console
    ) {}

    public record SignUpRequest(
            @NotBlank
            String email,

            @NotBlank
            String password,
            String firstName,
            String lastName
    ) {}
}
