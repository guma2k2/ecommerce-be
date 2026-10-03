package com.yas.system.payment.internal.gateway;

import com.stripe.model.Event;
import com.yas.system.payment.api.dto.CheckoutItemDto;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.entity.Payment;

import java.util.List;

public interface PaymentGateway {

    CheckoutSessionResponse createCheckoutSession(Payment payment, String customerEmail, List<CheckoutItemDto> items);

    Event constructWebhookEvent(String payload, String signatureHeader);
}
