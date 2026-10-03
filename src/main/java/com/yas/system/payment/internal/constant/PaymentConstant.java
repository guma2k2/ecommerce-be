package com.yas.system.payment.internal.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentConstant {

    public static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";
    public static final String DEFAULT_CURRENCY = "usd";
    public static final String MIN_CHECKOUT_AMOUNT = "0.50";
    public static final long CENTS_PER_UNIT = 100L;
    public static final BigDecimal CENTS_MULTIPLIER = BigDecimal.valueOf(100);
    public static final int STRIPE_MAX_DESCRIPTION_LENGTH = 500;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class WebhookTopic {
        public static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
        public static final String CHECKOUT_SESSION_EXPIRED = "checkout.session.expired";
        public static final String PAYMENT_INTENT_PAYMENT_FAILED = "payment_intent.payment_failed";
        public static final String PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class MetadataKey {
        public static final String PAYMENT_ID = "payment_id";
        public static final String CUSTOMER_ID = "customer_id";
        public static final String ORDER_ID = "order_id";
        public static final String PRODUCT_ID = "product_id";
        public static final String VARIANT_ID = "variant_id";
        public static final String SKU = "sku";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LineItem {
        public static final String DEFAULT_NAME = "Item";
        public static final String ORDER_PREFIX = "Order #";
        public static final long MIN_QUANTITY = 1L;
    }
}
