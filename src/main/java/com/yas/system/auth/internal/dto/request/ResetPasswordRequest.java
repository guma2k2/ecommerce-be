package com.yas.system.auth.internal.dto.request;

public record ResetPasswordRequest(
        String token,
        String password
) {
}
