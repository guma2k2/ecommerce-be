package com.yas.system.auth.internal.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
