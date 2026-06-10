package com.yas.system.auth.internal.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {

    private static final long maxAgeSeconds = 60 * 60 * 24 * 7; // 7 days
    public static ResponseCookie createRefreshTokenCookie(String token, boolean secure) {
        return ResponseCookie.from("refresh_token", token)
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(Duration.ofSeconds(maxAgeSeconds))
            .sameSite(secure ? "Strict" : "Lax")
            .build();
    }
    public static ResponseCookie deleteRefreshTokenCookie(boolean secure) {
        return ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(Duration.ZERO)
            .sameSite(secure ? "Strict" : "Lax")
            .build();
    }
}
