package com.yas.system.notification.events;

public record ResetPasswordEvent (
        String email,
        String name,
        String resetUrl,
        int expirationMinutes
) {
}
