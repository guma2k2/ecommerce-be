package com.yas.system.payment.internal.gateway;

import com.stripe.model.Event;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.entity.Payment;

public interface PaymentGateway {

    CheckoutSessionResponse createCheckoutSession(Payment payment, String customerEmail);

    Event constructWebhookEvent(String payload, String signatureHeader);
}
