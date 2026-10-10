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
import com.yas.system.payment.api.dto.CheckoutItemDto;
import com.yas.system.payment.internal.config.StripeProperties;
import com.yas.system.payment.internal.constant.PaymentConstant;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StripePaymentGateway implements PaymentGateway {

    StripeProperties stripeProperties;

    @Override
    public CheckoutSessionResponse createCheckoutSession(Payment payment, String customerEmail, List<CheckoutItemDto> items) {
        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(stripeProperties.successUrl())
                .setCancelUrl(stripeProperties.cancelUrl())
                .putMetadata(PaymentConstant.MetadataKey.PAYMENT_ID, String.valueOf(payment.getId()))
                .putMetadata(PaymentConstant.MetadataKey.CUSTOMER_ID, payment.getCustomerId());

        if (payment.getOrderId() != null) {
            paramsBuilder.putMetadata(PaymentConstant.MetadataKey.ORDER_ID, payment.getOrderId());
        }

        if (customerEmail != null && !customerEmail.isBlank()) {
            paramsBuilder.setCustomerEmail(customerEmail);
        }

        if (items != null && !items.isEmpty()) {
            log.info("Creating Stripe Checkout session for payment ID: {} (orderId: {}) with {} itemized line items",
                    payment.getId(), payment.getOrderId(), items.size());

            for (CheckoutItemDto item : items) {
                SessionCreateParams.LineItem.Builder lineItemBuilder = SessionCreateParams.LineItem.builder()
                        .setQuantity((long) Math.max(PaymentConstant.LineItem.MIN_QUANTITY, item.quantity()));

                if (item.stripePriceId() != null && !item.stripePriceId().isBlank()) {
                    log.debug("Adding Stripe LineItem with synced priceId='{}', qty={}", item.stripePriceId(), item.quantity());
                    lineItemBuilder.setPrice(item.stripePriceId());
                } else {
                    long unitAmountInCents = item.unitPrice() != null
                            ? item.unitPrice().multiply(PaymentConstant.CENTS_MULTIPLIER).longValue()
                            : 0L;

                    log.debug("Adding Stripe LineItem with dynamic priceData: name='{}', unitAmountInCents={}, qty={}, thumb='{}'",
                            item.productName(), unitAmountInCents, item.quantity(), item.thumbnailUrl());

                    SessionCreateParams.LineItem.PriceData.ProductData.Builder productDataBuilder =
                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.productName() != null && !item.productName().isBlank()
                                            ? item.productName()
                                            : PaymentConstant.LineItem.DEFAULT_NAME);

                    if (item.thumbnailUrl() != null && !item.thumbnailUrl().isBlank()) {
                        productDataBuilder.addImage(item.thumbnailUrl());
                    }

                    SessionCreateParams.LineItem.PriceData priceData =
                            SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(payment.getCurrency().toLowerCase())
                                    .setUnitAmount(unitAmountInCents)
                                    .setProductData(productDataBuilder.build())
                                    .build();

                    lineItemBuilder.setPriceData(priceData);
                }

                paramsBuilder.addLineItem(lineItemBuilder.build());
            }
        } else {
            long unitAmountInCents = payment.getAmount()
                    .multiply(PaymentConstant.CENTS_MULTIPLIER)
                    .longValue();

            log.info("Creating Stripe Checkout session with generic order-level LineItem (amount: {} {})",
                    payment.getAmount(), payment.getCurrency());

            SessionCreateParams.LineItem.PriceData.ProductData productData =
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                            .setName(PaymentConstant.LineItem.ORDER_PREFIX + (payment.getOrderId() != null ? payment.getOrderId() : payment.getId()))
                            .build();

            SessionCreateParams.LineItem.PriceData priceData =
                    SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(payment.getCurrency().toLowerCase())
                            .setUnitAmount(unitAmountInCents)
                            .setProductData(productData)
                            .build();

            SessionCreateParams.LineItem lineItem =
                    SessionCreateParams.LineItem.builder()
                            .setQuantity(PaymentConstant.LineItem.MIN_QUANTITY)
                            .setPriceData(priceData)
                            .build();

            paramsBuilder.addLineItem(lineItem);
        }

        try {
            StripeClient client = new StripeClient(stripeProperties.apiKey());
            Session session = client.checkout().sessions().create(paramsBuilder.build());
            log.info("Successfully created Stripe Checkout session: sessionId={}, url={}", session.getId(), session.getUrl());
            return new CheckoutSessionResponse(payment.getId(), session.getId(), session.getUrl());
        } catch (StripeException e) {
            log.error("Failed to create Stripe Checkout session: {}", e.getMessage(), e);
            throw new ApplicationException(ErrorCode.STRIPE_CHECKOUT_FAILED, e.getMessage());
        }
    }

    @Override
    public Event constructWebhookEvent(String payload, String signatureHeader) {
        if (signatureHeader == null || signatureHeader.isBlank()) {
            log.warn("Webhook request rejected: missing '{}' header", PaymentConstant.STRIPE_SIGNATURE_HEADER);
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
