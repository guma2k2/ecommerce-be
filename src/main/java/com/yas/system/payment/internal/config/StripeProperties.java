package com.yas.system.payment.internal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.stripe")
public record StripeProperties(
        String apiKey,
        String webhookSecret,
        String successUrl,
        String cancelUrl,
        Boolean syncEnabled
) {
    public boolean isSyncEnabled() {
        return Boolean.TRUE.equals(syncEnabled) && apiKey != null && !apiKey.isBlank();
    }
}
