package com.yas.system.payment.enumeration;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;

/**
 * Supported payment methods within the payment system.
 */
public enum PaymentMethod {
    /**
     * Online payment processed through the Stripe payment gateway.
     */
    STRIPE,

    /**
     * Cash on Delivery; payment collected in person upon order delivery.
     */
    COD;

    public static PaymentMethod from(String value) {
        if (value == null || value.isBlank()) {
            throw new ApplicationException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED, "null");
        }
        try {
            return PaymentMethod.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED, value);
        }
    }
}

