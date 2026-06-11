package com.yas.system.auth.internal.dto.request;

public record OutboundAuthenticationRequest(
        String code,
        String state,
        String registryId
) {
}
