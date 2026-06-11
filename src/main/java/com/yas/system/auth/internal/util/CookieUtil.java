package com.yas.system.auth.internal.util;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtil {

    private static final long maxAgeSeconds = 60 * 60 * 24 * 7; // 7 days
    public static ResponseCookie createCookie(String name, String value, boolean secure) {
        return ResponseCookie.from(name, value)
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(Duration.ofSeconds(maxAgeSeconds))
            .sameSite(secure ? "Strict" : "Lax")
            .build();
    }
    public static ResponseCookie deleteCookie(String name, boolean secure) {
        return ResponseCookie.from(name, "")
            .httpOnly(true)
            .secure(secure)
            .path("/")
            .maxAge(Duration.ZERO)
            .sameSite(secure ? "Strict" : "Lax")
            .build();
    }
}
