package com.yas.system.payment.internal.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentConstant {

    public static final String STRIPE_SIGNATURE_HEADER = "Stripe-Signature";
    public static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    public static final String IDEMPOTENCY_CACHE_PREFIX = "payment:idempotency:";
    public static final String IDEMPOTENCY_IN_PROGRESS = "IN_PROGRESS";
    public static final String DEFAULT_CURRENCY = "usd";
    public static final String MIN_CHECKOUT_AMOUNT = "0.50";
    public static final long CENTS_PER_UNIT = 100L;
    public static final BigDecimal CENTS_MULTIPLIER = BigDecimal.valueOf(100);
    public static final int STRIPE_MAX_DESCRIPTION_LENGTH = 500;
    public static final long RECONCILIATION_STALE_MINUTES = 10L;
    public static final long RECONCILIATION_WINDOW_HOURS = 24L;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class WebhookTopic {
        public static final String CHECKOUT_SESSION_COMPLETED = "checkout.session.completed";
        public static final String CHECKOUT_SESSION_EXPIRED = "checkout.session.expired";
        public static final String PAYMENT_INTENT_PAYMENT_FAILED = "payment_intent.payment_failed";
        public static final String PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class StripeStatus {
        public static final String SESSION_STATUS_COMPLETE = "complete";
        public static final String SESSION_STATUS_EXPIRED = "expired";
        public static final String SESSION_STATUS_OPEN = "open";

        public static final String PAYMENT_STATUS_PAID = "paid";
        public static final String PAYMENT_STATUS_UNPAID = "unpaid";
        public static final String PAYMENT_STATUS_NO_PAYMENT_REQUIRED = "no_payment_required";

        public static final String INTENT_STATUS_SUCCEEDED = "succeeded";
        public static final String INTENT_STATUS_CANCELED = "canceled";
        public static final String INTENT_STATUS_REQUIRES_PAYMENT_METHOD = "requires_payment_method";
        public static final String INTENT_STATUS_REQUIRES_ACTION = "requires_action";
        public static final String INTENT_STATUS_PROCESSING = "processing";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class MetadataKey {
        public static final String PAYMENT_ID = "payment_id";
        public static final String CUSTOMER_ID = "customer_id";
        public static final String ORDER_ID = "order_id";
        public static final String PRODUCT_ID = "product_id";
        public static final String VARIANT_ID = "variant_id";
        public static final String SKU = "sku";
        public static final String PROMOTION_ID = "promotion_id";
        public static final String COUPON_CODE = "coupon_code";
        public static final String SLUG = "slug";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class LineItem {
        public static final String DEFAULT_NAME = "Item";
        public static final String ORDER_PREFIX = "Order #";
        public static final long MIN_QUANTITY = 1L;
    }
}
