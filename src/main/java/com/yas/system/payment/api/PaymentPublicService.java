package com.yas.system.payment.api;

import com.yas.system.payment.api.dto.CheckoutItemDto;
import com.yas.system.payment.api.dto.PaymentPublicDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentPublicService {

    Optional<PaymentPublicDto> getPaymentById(Long paymentId);

    Optional<PaymentPublicDto> getPaymentByOrderId(String orderId);

    String createCheckoutSessionUrl(String customerId, String customerEmail, String orderId, BigDecimal amount, String currency, List<CheckoutItemDto> items);
}

