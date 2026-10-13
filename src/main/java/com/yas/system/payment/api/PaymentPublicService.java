package com.yas.system.payment.api;

import com.yas.system.payment.api.dto.CheckoutItemDto;
import com.yas.system.payment.api.dto.PaymentPublicDto;
import com.yas.system.payment.internal.enumeration.PaymentMethod;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentPublicService {

    Optional<PaymentPublicDto> getPaymentById(UUID paymentId);

    Optional<PaymentPublicDto> getPaymentByOrderId(String orderId);

    String createCheckoutSessionUrl(String customerId, String customerEmail, String orderId, BigDecimal amount, String currency, List<CheckoutItemDto> items);

    String createCheckoutSessionUrl(PaymentMethod paymentMethod, String customerId, String customerEmail, String orderId, BigDecimal amount, String currency, List<CheckoutItemDto> items);
}
