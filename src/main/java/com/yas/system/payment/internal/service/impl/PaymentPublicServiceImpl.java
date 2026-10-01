package com.yas.system.payment.internal.service.impl;

import com.yas.system.payment.api.PaymentPublicService;
import com.yas.system.payment.api.dto.PaymentPublicDto;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.repository.PaymentRepository;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentPublicServiceImpl implements PaymentPublicService {

    PaymentRepository paymentRepository;
    PaymentGateway paymentGateway;

    @Override
    @Transactional
    public String createCheckoutSessionUrl(String customerId, String customerEmail, String orderId, BigDecimal amount, String currency) {
        Payment payment = Payment.builder()
                .customerId(customerId)
                .orderId(orderId)
                .amount(amount)
                .currency(currency.toUpperCase())
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.STRIPE)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        var sessionResponse = paymentGateway.createCheckoutSession(savedPayment, customerEmail);
        savedPayment.setStripeSessionId(sessionResponse.sessionId());
        paymentRepository.save(savedPayment);

        return sessionResponse.checkoutUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentPublicDto> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).map(this::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentPublicDto> getPaymentByOrderId(String orderId) {
        return paymentRepository.findAll().stream()
                .filter(p -> orderId.equals(p.getOrderId()))
                .findFirst()
                .map(this::toDto);
    }

    private PaymentPublicDto toDto(Payment payment) {
        return new PaymentPublicDto(
                payment.getId(),
                payment.getCustomerId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getMethod().name(),
                payment.getStripeSessionId(),
                payment.getStripePaymentIntentId(),
                payment.getCreatedAt()
        );
    }
}
