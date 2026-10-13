package com.yas.system.payment.internal.service;

import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.dto.request.CreateCheckoutSessionRequest;
import com.yas.system.payment.internal.dto.request.RefundPaymentRequest;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.dto.response.PaymentLedgerResponse;
import com.yas.system.payment.internal.dto.response.PaymentResponse;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.gateway.dto.RefundResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface PaymentService {

    CheckoutSessionResponse createCheckoutSession(AuthUser customer, String idempotencyKey, CreateCheckoutSessionRequest request);

    void handleWebhookEvent(PaymentMethod provider, Map<String, String> headers, String payload);

    RefundResult refundPayment(AuthUser customer, UUID paymentId, RefundPaymentRequest request, String idempotencyKey);

    PaymentResponse getPaymentById(AuthUser customer, UUID paymentId);

    List<PaymentResponse> getCustomerPayments(AuthUser customer);

    List<PaymentLedgerResponse> getPaymentLedger(AuthUser customer, UUID paymentId);
}
