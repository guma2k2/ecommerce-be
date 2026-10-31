package com.yas.system.payment.internal.gateway;

import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.enumeration.PaymentMethod;
import com.yas.system.payment.internal.gateway.dto.PaymentCheckoutContext;
import com.yas.system.payment.internal.gateway.dto.PaymentStatusResult;
import com.yas.system.payment.internal.gateway.dto.RefundResult;
import com.yas.system.payment.internal.gateway.dto.WebhookResult;

import java.math.BigDecimal;
import java.util.Map;

public interface PaymentGateway {

    PaymentMethod getPaymentMethod();

    CheckoutSessionResponse createCheckoutSession(Payment payment, PaymentCheckoutContext context);

    WebhookResult processWebhook(Map<String, String> headers, String payload);

    PaymentStatusResult queryTransactionStatus(Payment payment);

    RefundResult refund(Payment payment, BigDecimal amount, String reason, String idempotencyKey);
}
