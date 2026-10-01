package com.yas.system.payment.internal.service;

import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.dto.request.CreateCheckoutSessionRequest;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    CheckoutSessionResponse createCheckoutSession(AuthUser customer, CreateCheckoutSessionRequest request);

    void handleWebhookEvent(String payload, String signatureHeader);

    PaymentResponse getPaymentById(AuthUser customer, Long paymentId);

    List<PaymentResponse> getCustomerPayments(AuthUser customer);
}
