package com.yas.system.payment.internal.dto.response;

public record CheckoutSessionResponse(
        Long paymentId,
        String sessionId,
        String checkoutUrl
) {
}
