package com.yas.system.payment.internal.gateway.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.payment.internal.config.StripeProperties;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StripePaymentGateway implements PaymentGateway {

    StripeProperties stripeProperties;

    @Override
    public CheckoutSessionResponse createCheckoutSession(Payment payment, String customerEmail) {
        long unitAmountInCents = payment.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        SessionCreateParams.LineItem.PriceData.ProductData productData =
                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Order #" + (payment.getOrderId() != null ? payment.getOrderId() : payment.getId()))
                        .build();

        SessionCreateParams.LineItem.PriceData priceData =
                SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency(payment.getCurrency().toLowerCase())
                        .setUnitAmount(unitAmountInCents)
                        .setProductData(productData)
                        .build();

        SessionCreateParams.LineItem lineItem =
                SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(priceData)
                        .build();

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeProperties.successUrl())
                .setCancelUrl(stripeProperties.cancelUrl())
                .addLineItem(lineItem)
                .putMetadata("payment_id", String.valueOf(payment.getId()))
                .putMetadata("customer_id", payment.getCustomerId());

        if (payment.getOrderId() != null) {
            paramsBuilder.putMetadata("order_id", String.valueOf(payment.getOrderId()));
        }

        if (customerEmail != null && !customerEmail.isBlank()) {
            paramsBuilder.setCustomerEmail(customerEmail);
        }

        try {
            StripeClient client = new StripeClient(stripeProperties.apiKey());
            Session session = client.checkout().sessions().create(paramsBuilder.build());
            return new CheckoutSessionResponse(payment.getId(), session.getId(), session.getUrl());
        } catch (StripeException e) {
            log.error("Failed to create Stripe Checkout session: {}", e.getMessage(), e);
            throw new ApplicationException(ErrorCode.STRIPE_CHECKOUT_FAILED, e.getMessage());
        }
    }

    @Override
    public Event constructWebhookEvent(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("Webhook request rejected: missing 'Stripe-Signature' header");
            throw new ApplicationException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
        if (stripeProperties.webhookSecret() == null || stripeProperties.webhookSecret().isBlank()) {
            log.error("Webhook request rejected: 'app.stripe.webhookSecret' is missing or blank in configuration!");
            throw new ApplicationException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
        try {
            return Webhook.constructEvent(payload, signatureHeader, stripeProperties.webhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}. (Hint: verify STRIPE_WEBHOOK_SECRET matches 'stripe listen')", e.getMessage());
            throw new ApplicationException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
    }
}
