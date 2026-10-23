package com.yas.system.payment.internal.service.impl;

import com.yas.system.payment.api.PaymentPublicService;
import com.yas.system.payment.api.dto.CheckoutItemDto;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.event.RefundCompletedEvent;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.gateway.PaymentGatewayResolver;
import com.yas.system.payment.internal.gateway.dto.PaymentCheckoutContext;
import com.yas.system.payment.internal.gateway.dto.RefundResult;
import com.yas.system.payment.internal.repository.PaymentRepository;
import com.yas.system.payment.internal.service.PaymentLedgerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentPublicServiceImpl implements PaymentPublicService {

    PaymentRepository paymentRepository;
    PaymentGatewayResolver paymentGatewayResolver;
    PaymentLedgerService paymentLedgerService;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public String createCheckoutSessionUrl(PaymentMethod paymentMethod, String customerId, String customerEmail,
                                           String orderId, BigDecimal amount, String currency,
                                           List<CheckoutItemDto> items,
                                           String stripePromotionCodeId, String stripeCouponId) {
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
        // Todo: returnUrl and cancelUrl should dynamic on request ?
        PaymentCheckoutContext context = new PaymentCheckoutContext(
                customerEmail, items, null, null, stripePromotionCodeId, stripeCouponId
        );
        var sessionResponse = gateway.createCheckoutSession(savedPayment, context);

        savedPayment.setGatewaySessionId(sessionResponse.sessionId());
        paymentRepository.save(savedPayment);

        paymentLedgerService.recordInitialization(savedPayment);

        return sessionResponse.checkoutUrl();
    }

    @Override
    @Transactional
    public void refundByOrderId(String orderId, String customerEmail, String reason) {
        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(orderId);
        if (paymentOpt.isEmpty()) {
            log.info("No payment found for orderId {}. Skipping auto-refund.", orderId);
            return;
        }

        Payment payment = paymentOpt.get();

        if (payment.getStatus() != PaymentStatus.SUCCEEDED
                && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            log.info("Payment {} for order {} is in status {}. No refund needed.",
                    payment.getId(), orderId, payment.getStatus());
            return;
        }

        BigDecimal refundableAmount = payment.getRefundableAmount();
        if (refundableAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.info("Payment {} for order {} has no remaining refundable amount. Skipping.", payment.getId(), orderId);
            return;
        }

        String idempotencyKey = "cancel-refund-" + orderId;
        PaymentGateway gateway = paymentGatewayResolver.resolve(payment.getMethod());
        RefundResult refundResult = gateway.refund(payment, refundableAmount, reason, idempotencyKey);

        if (!refundResult.successful()) {
            log.error("Auto-refund failed for payment {} (order {}): {}", payment.getId(), orderId, refundResult.failureReason());
            return;
        }

        BigDecimal newRefundedAmount = payment.getRefundedAmount().add(refundableAmount);
        payment.setRefundedAmount(newRefundedAmount);
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        paymentLedgerService.recordRefund(payment, refundableAmount, refundResult.gatewayRefundId(), reason);

        log.info("Auto-refund succeeded for payment {} (order {}): amount={} {}",
                payment.getId(), orderId, refundableAmount, payment.getCurrency());

        eventPublisher.publishEvent(new RefundCompletedEvent(
                payment.getId(),
                orderId,
                payment.getCustomerId(),
                customerEmail,
                refundableAmount,
                payment.getAmount(),
                payment.getCurrency(),
                PaymentStatus.REFUNDED,
                reason
        ));
    }
}
