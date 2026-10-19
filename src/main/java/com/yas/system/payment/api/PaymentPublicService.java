package com.yas.system.payment.api;

import com.yas.system.payment.api.dto.CheckoutItemDto;
import com.yas.system.payment.internal.enumeration.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentPublicService {

    String createCheckoutSessionUrl(PaymentMethod paymentMethod, String customerId, String customerEmail,
                                    String orderId, BigDecimal amount, String currency, List<CheckoutItemDto> items);

    /**
     * Triggers a full refund for the payment associated with the given order.
     * Used internally by the order module when an order is cancelled after payment was captured.
     * No-ops if no SUCCEEDED payment exists for the order.
     */
    void refundByOrderId(String orderId, String customerEmail, String reason);
}
