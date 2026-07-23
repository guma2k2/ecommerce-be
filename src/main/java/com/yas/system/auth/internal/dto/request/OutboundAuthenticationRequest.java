package com.yas.system.auth.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OutboundAuthenticationRequest(
        @NotBlank String code,
        @NotBlank String state,
        @NotBlank String registrationId
) {
}

