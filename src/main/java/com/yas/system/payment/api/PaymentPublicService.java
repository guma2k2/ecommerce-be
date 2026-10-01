package com.yas.system.payment.api;

import com.yas.system.payment.api.dto.PaymentPublicDto;

import java.util.Optional;

public interface PaymentPublicService {

    Optional<PaymentPublicDto> getPaymentById(Long paymentId);

    Optional<PaymentPublicDto> getPaymentByOrderId(String orderId);

    String createCheckoutSessionUrl(String customerId, String customerEmail, String orderId, java.math.BigDecimal amount, String currency);
}
