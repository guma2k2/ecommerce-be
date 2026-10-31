package com.yas.system.payment.internal.gateway.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.payment.api.dto.CheckoutItemDto;
import com.yas.system.payment.internal.config.StripeProperties;
import com.yas.system.payment.internal.constant.PaymentConstant;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.enumeration.PaymentMethod;
import com.yas.system.payment.enumeration.PaymentStatus;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.gateway.dto.PaymentCheckoutContext;
import com.yas.system.payment.internal.gateway.dto.PaymentStatusResult;
import com.yas.system.payment.internal.gateway.dto.RefundResult;
import com.yas.system.payment.internal.gateway.dto.WebhookResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StripePaymentGateway implements PaymentGateway {

    StripeProperties stripeProperties;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.STRIPE;
    }

    @Override
    public CheckoutSessionResponse createCheckoutSession(Payment payment, PaymentCheckoutContext context) {
        String successUrl = context != null && context.returnUrl() != null && !context.returnUrl().isBlank()
                ? context.returnUrl()
                : stripeProperties.successUrl();

        String cancelUrl = context != null && context.cancelUrl() != null && !context.cancelUrl().isBlank()
                ? context.cancelUrl()
                : stripeProperties.cancelUrl();

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .putMetadata(PaymentConstant.MetadataKey.PAYMENT_ID, String.valueOf(payment.getId()))
                .putMetadata(PaymentConstant.MetadataKey.CUSTOMER_ID, payment.getCustomerId());

        if (payment.getOrderId() != null) {
            paramsBuilder.putMetadata(PaymentConstant.MetadataKey.ORDER_ID, payment.getOrderId());
        }

        if (context != null && context.customerEmail() != null && !context.customerEmail().isBlank()) {
            paramsBuilder.setCustomerEmail(context.customerEmail());
        }

        if (context != null && context.stripePromotionCodeId() != null && !context.stripePromotionCodeId().isBlank()) {
            paramsBuilder.addDiscount(SessionCreateParams.Discount.builder().setPromotionCode(context.stripePromotionCodeId()).build());
        } else if (context != null && context.stripeCouponId() != null && !context.stripeCouponId().isBlank()) {
            paramsBuilder.addDiscount(SessionCreateParams.Discount.builder().setCoupon(context.stripeCouponId()).build());
        } else {
            paramsBuilder.setAllowPromotionCodes(true);
        }

        List<CheckoutItemDto> items = context != null ? context.items() : null;
        if (items != null && !items.isEmpty()) {
            log.info("Creating Stripe Checkout session for payment ID: {} (orderId: {}) with {} itemized line items",
                    payment.getId(), payment.getOrderId(), items.size());

            for (CheckoutItemDto item : items) {
                SessionCreateParams.LineItem.Builder lineItemBuilder = SessionCreateParams.LineItem.builder()
                        .setQuantity((long) Math.max(PaymentConstant.LineItem.MIN_QUANTITY, item.quantity()));

                if (item.stripePriceId() != null && !item.stripePriceId().isBlank()) {
                    lineItemBuilder.setPrice(item.stripePriceId());
                } else {
                    long unitAmountInCents = item.unitPrice() != null
                            ? item.unitPrice().multiply(PaymentConstant.CENTS_MULTIPLIER).longValue()
                            : 0L;

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
            RequestOptions.RequestOptionsBuilder optionsBuilder = RequestOptions.builder();
            if (payment.getIdempotencyKey() != null && !payment.getIdempotencyKey().isBlank()) {
                optionsBuilder.setIdempotencyKey("stripe_session_" + payment.getIdempotencyKey());
            }

            Session session = client.checkout().sessions().create(paramsBuilder.build(), optionsBuilder.build());
            log.info("Successfully created Stripe Checkout session: sessionId={}, url={}", session.getId(), session.getUrl());
            return new CheckoutSessionResponse(payment.getId(), session.getId(), session.getUrl());
        } catch (StripeException e) {
            log.error("Failed to create Stripe Checkout session: {}", e.getMessage(), e);
            throw new ApplicationException(ErrorCode.STRIPE_CHECKOUT_FAILED, e.getMessage());
        }
    }

    @Override
    public WebhookResult processWebhook(Map<String, String> headers, String payload) {
        String signatureHeader = findHeader(headers, PaymentConstant.STRIPE_SIGNATURE_HEADER);
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new ApplicationException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }
        if (stripeProperties.webhookSecret() == null || stripeProperties.webhookSecret().isBlank()) {
            throw new ApplicationException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeProperties.webhookSecret());
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            throw new ApplicationException(ErrorCode.INVALID_WEBHOOK_SIGNATURE);
        }

        String eventType = event.getType();
        String eventId = event.getId();
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);
        if (stripeObject == null) {
            try {
                stripeObject = dataObjectDeserializer.deserializeUnsafe();
            } catch (Exception e) {
                log.warn("deserializeUnsafe failed for event {}: {}", eventId, e.getMessage());
            }
        }

        return switch (eventType) {
            case PaymentConstant.WebhookTopic.CHECKOUT_SESSION_COMPLETED -> {
                if (stripeObject instanceof Session session) {
                    String txId = session.getPaymentIntent() != null ? session.getPaymentIntent() : session.getId();
                    String orderId = session.getMetadata() != null ? session.getMetadata().get(PaymentConstant.MetadataKey.ORDER_ID) : null;
                    yield new WebhookResult(true, eventId, txId, orderId, PaymentStatus.SUCCEEDED, null, payload);
                }
                yield new WebhookResult(false, eventId, null, null, null, "Failed to deserialize Session", payload);
            }
            case PaymentConstant.WebhookTopic.CHECKOUT_SESSION_EXPIRED -> {
                if (stripeObject instanceof Session session) {
                    String txId = session.getPaymentIntent() != null ? session.getPaymentIntent() : session.getId();
                    String orderId = session.getMetadata() != null ? session.getMetadata().get(PaymentConstant.MetadataKey.ORDER_ID) : null;
                    yield new WebhookResult(true, eventId, txId, orderId, PaymentStatus.CANCELLED, "Checkout session expired", payload);
                }
                yield new WebhookResult(false, eventId, null, null, null, "Failed to deserialize expired Session", payload);
            }
            case PaymentConstant.WebhookTopic.PAYMENT_INTENT_PAYMENT_FAILED -> {
                if (stripeObject instanceof PaymentIntent paymentIntent) {
                    String reason = paymentIntent.getLastPaymentError() != null ? paymentIntent.getLastPaymentError().getMessage() : "Payment intent failed";
                    String orderId = paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get(PaymentConstant.MetadataKey.ORDER_ID) : null;
                    yield new WebhookResult(true, eventId, paymentIntent.getId(), orderId, PaymentStatus.FAILED, reason, payload);
                }
                yield new WebhookResult(false, eventId, null, null, null, "Failed to deserialize PaymentIntent", payload);
            }
            default -> {
                log.debug("Ignored unhandled Stripe event type: {}", eventType);
                yield new WebhookResult(false, eventId, null, null, null, "Unhandled event type: " + eventType, payload);
            }
        };
    }

    @Override
    public PaymentStatusResult queryTransactionStatus(Payment payment) {
        StripeClient client = new StripeClient(stripeProperties.apiKey());

        try {
            if (payment.getGatewaySessionId() != null && !payment.getGatewaySessionId().isBlank()) {
                Session session = client.checkout().sessions().retrieve(payment.getGatewaySessionId());
                String status = session.getStatus();
                String paymentStatus = session.getPaymentStatus();
                String txId = session.getPaymentIntent() != null ? session.getPaymentIntent() : session.getId();

                PaymentStatus mappedStatus = mapSessionStatus(status, paymentStatus);
                String failureReason = mappedStatus == PaymentStatus.CANCELLED ? "Session expired" : null;

                return new PaymentStatusResult(mappedStatus, txId, payment.getAmount(), payment.getCurrency(), failureReason, status);
            } else if (payment.getGatewayTransactionId() != null && !payment.getGatewayTransactionId().isBlank()) {
                PaymentIntent intent = client.paymentIntents().retrieve(payment.getGatewayTransactionId());
                String status = intent.getStatus();

                PaymentStatus mappedStatus = mapPaymentIntentStatus(status);
                String failureReason = resolvePaymentIntentFailureReason(status, intent);

                return new PaymentStatusResult(mappedStatus, intent.getId(), payment.getAmount(), payment.getCurrency(), failureReason, status);
            }
        } catch (StripeException e) {
            log.warn("Error querying transaction status from Stripe for payment {}: {}", payment.getId(), e.getMessage());
            return new PaymentStatusResult(payment.getStatus(), payment.getGatewayTransactionId(), payment.getAmount(), payment.getCurrency(), e.getMessage(), "ERROR");
        }

        return new PaymentStatusResult(payment.getStatus(), payment.getGatewayTransactionId(), payment.getAmount(), payment.getCurrency(), null, "UNKNOWN");
    }

    private PaymentStatus mapSessionStatus(String sessionStatus, String paymentStatus) {
        if (PaymentConstant.StripeStatus.PAYMENT_STATUS_PAID.equalsIgnoreCase(paymentStatus)
                || PaymentConstant.StripeStatus.SESSION_STATUS_COMPLETE.equalsIgnoreCase(sessionStatus)) {
            return PaymentStatus.SUCCEEDED;
        }
        if (PaymentConstant.StripeStatus.SESSION_STATUS_EXPIRED.equalsIgnoreCase(sessionStatus)) {
            return PaymentStatus.CANCELLED;
        }
        return PaymentStatus.PENDING;
    }

    private PaymentStatus mapPaymentIntentStatus(String intentStatus) {
        if (intentStatus == null) {
            return PaymentStatus.PENDING;
        }
        return switch (intentStatus.toLowerCase()) {
            case PaymentConstant.StripeStatus.INTENT_STATUS_SUCCEEDED -> PaymentStatus.SUCCEEDED;
            case PaymentConstant.StripeStatus.INTENT_STATUS_CANCELED -> PaymentStatus.CANCELLED;
            case PaymentConstant.StripeStatus.INTENT_STATUS_REQUIRES_PAYMENT_METHOD -> PaymentStatus.FAILED;
            default -> PaymentStatus.PENDING;
        };
    }

    private String resolvePaymentIntentFailureReason(String intentStatus, PaymentIntent intent) {
        if (PaymentConstant.StripeStatus.INTENT_STATUS_CANCELED.equalsIgnoreCase(intentStatus)) {
            return "Intent canceled";
        }
        if (PaymentConstant.StripeStatus.INTENT_STATUS_REQUIRES_PAYMENT_METHOD.equalsIgnoreCase(intentStatus)) {
            return intent.getLastPaymentError() != null ? intent.getLastPaymentError().getMessage() : "Payment failed";
        }
        return null;
    }

    @Override
    public RefundResult refund(Payment payment, BigDecimal amount, String reason, String idempotencyKey) {
        if (payment.getGatewayTransactionId() == null || payment.getGatewayTransactionId().isBlank()) {
            throw new ApplicationException(ErrorCode.PAYMENT_GATEWAY_ERROR, "Cannot refund payment without gateway transaction ID");
        }

        StripeClient client = new StripeClient(stripeProperties.apiKey());
        long amountInCents = amount.multiply(PaymentConstant.CENTS_MULTIPLIER).longValue();

        RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setPaymentIntent(payment.getGatewayTransactionId())
                .setAmount(amountInCents);

        if (reason != null && !reason.isBlank()) {
            paramsBuilder.putMetadata("reason", reason);
        }

        RequestOptions.RequestOptionsBuilder optionsBuilder = RequestOptions.builder();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            optionsBuilder.setIdempotencyKey("stripe_refund_" + idempotencyKey);
        }

        try {
            Refund refund = client.refunds().create(paramsBuilder.build(), optionsBuilder.build());
            log.info("Successfully executed Stripe refund: refundId={}, amount={}, paymentId={}",
                    refund.getId(), amount, payment.getId());
            return new RefundResult(true, refund.getId(), amount, null);
        } catch (StripeException e) {
            log.error("Failed to execute Stripe refund for payment {}: {}", payment.getId(), e.getMessage(), e);
            throw new ApplicationException(ErrorCode.PAYMENT_GATEWAY_ERROR, e.getMessage());
        }
    }

    private String findHeader(Map<String, String> headers, String target) {
        if (headers == null || target == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (target.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
