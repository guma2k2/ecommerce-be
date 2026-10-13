package com.yas.system.payment.internal.enumeration;

/**
 * Represents the lifecycle status of a payment transaction.
 */
public enum PaymentStatus {
    /**
     * Payment has been initiated and is awaiting customer action or gateway processing.
     */
    PENDING,

    /**
     * Payment has been successfully authorized and captured.
     */
    SUCCEEDED,

    /**
     * Payment authorization or capture failed.
     */
    FAILED,

    /**
     * Payment was cancelled by the user or the checkout session expired.
     */
    CANCELLED,

    /**
     * A partial amount of the total payment has been refunded.
     */
    PARTIALLY_REFUNDED,

    /**
     * The full payment amount has been refunded.
     */
    REFUNDED
}
