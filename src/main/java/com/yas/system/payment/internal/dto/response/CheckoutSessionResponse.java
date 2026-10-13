package com.yas.system.payment.internal.dto.response;

import java.util.UUID;

public record CheckoutSessionResponse(
        UUID paymentId,
        String sessionId,
        String checkoutUrl
) {
}
