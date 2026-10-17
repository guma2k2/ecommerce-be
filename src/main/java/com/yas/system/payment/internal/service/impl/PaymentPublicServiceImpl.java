package com.yas.system.payment.internal.service.impl;

import com.yas.system.payment.api.PaymentPublicService;
import com.yas.system.payment.api.dto.CheckoutItemDto;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.gateway.PaymentGatewayResolver;
import com.yas.system.payment.internal.gateway.dto.PaymentCheckoutContext;
import com.yas.system.payment.internal.repository.PaymentRepository;
import com.yas.system.payment.internal.service.PaymentLedgerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentPublicServiceImpl implements PaymentPublicService {

    PaymentRepository paymentRepository;
    PaymentGatewayResolver paymentGatewayResolver;
    PaymentLedgerService paymentLedgerService;

    @Override
    @Transactional
    public String createCheckoutSessionUrl(PaymentMethod paymentMethod, String customerId, String customerEmail, String orderId, BigDecimal amount, String currency, List<CheckoutItemDto> items) {
        PaymentMethod method = paymentMethod != null ? paymentMethod : PaymentMethod.STRIPE;

        Payment payment = Payment.builder()
                .customerId(customerId)
                .orderId(orderId)
                .amount(amount)
                .refundedAmount(BigDecimal.ZERO)
                .currency(currency.toUpperCase())
                .status(PaymentStatus.PENDING)
                .method(method)
                .idempotencyKey(UUID.randomUUID().toString())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        PaymentGateway gateway = paymentGatewayResolver.resolve(method);
        PaymentCheckoutContext context = new PaymentCheckoutContext(customerEmail, items, null, null);
        var sessionResponse = gateway.createCheckoutSession(savedPayment, context);

        savedPayment.setGatewaySessionId(sessionResponse.sessionId());
        paymentRepository.save(savedPayment);

        paymentLedgerService.recordInitialization(savedPayment);

        return sessionResponse.checkoutUrl();
    }

}
