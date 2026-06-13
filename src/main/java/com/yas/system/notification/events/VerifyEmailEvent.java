package com.yas.system.notification.events;

public record VerifyEmailEvent(
        String email,
        String name,
        String code,
        int expirationMinutes
) {
}
