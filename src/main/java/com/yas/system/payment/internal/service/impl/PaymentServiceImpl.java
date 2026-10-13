package com.yas.system.payment.internal.service.impl;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.dto.request.CreateCheckoutSessionRequest;
import com.yas.system.payment.internal.dto.request.RefundPaymentRequest;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.dto.response.PaymentLedgerResponse;
import com.yas.system.payment.internal.dto.response.PaymentResponse;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.BalanceType;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.event.PaymentCompletedEvent;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.gateway.PaymentGatewayResolver;
import com.yas.system.payment.internal.gateway.dto.PaymentCheckoutContext;
import com.yas.system.payment.internal.gateway.dto.RefundResult;
import com.yas.system.payment.internal.gateway.dto.WebhookResult;
import com.yas.system.payment.internal.repository.PaymentRepository;
import com.yas.system.payment.internal.service.PaymentIdempotencyService;
import com.yas.system.payment.internal.service.PaymentLedgerService;
import com.yas.system.payment.internal.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentServiceImpl implements PaymentService {

    PaymentRepository paymentRepository;
    PaymentGatewayResolver paymentGatewayResolver;
    PaymentIdempotencyService paymentIdempotencyService;
    PaymentLedgerService paymentLedgerService;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CheckoutSessionResponse createCheckoutSession(AuthUser customer, String idempotencyKey, CreateCheckoutSessionRequest request) {
        String effectiveIdempotencyKey = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString();

        Optional<CheckoutSessionResponse> cached = paymentIdempotencyService.checkAndLock(effectiveIdempotencyKey);
        if (cached.isPresent()) {
            log.info("Returning cached checkout session for idempotency key: {}", effectiveIdempotencyKey);
            return cached.get();
        }

        try {
            PaymentMethod method = PaymentMethod.from(request.paymentMethod());
            Payment payment = Payment.builder()
                    .customerId(customer.id())
                    .orderId(request.orderId())
                    .amount(request.amount())
                    .refundedAmount(BigDecimal.ZERO)
                    .currency(request.currency().toUpperCase())
                    .status(PaymentStatus.PENDING)
                    .method(method)
                    .idempotencyKey(effectiveIdempotencyKey)
                    .build();

            Payment savedPayment = paymentRepository.save(payment);

            PaymentGateway gateway = paymentGatewayResolver.resolve(method);
            PaymentCheckoutContext context = new PaymentCheckoutContext(
                    customer.email(),
                    List.of(),
                    request.returnUrl(),
                    request.cancelUrl()
            );

            CheckoutSessionResponse sessionResponse = gateway.createCheckoutSession(savedPayment, context);

            savedPayment.setGatewaySessionId(sessionResponse.sessionId());
            paymentRepository.save(savedPayment);

            paymentLedgerService.recordInitialization(savedPayment);
            paymentIdempotencyService.saveCompleted(effectiveIdempotencyKey, sessionResponse);

            return sessionResponse;
        } catch (Exception e) {
            paymentIdempotencyService.unlock(effectiveIdempotencyKey);
            throw e;
        }
    }

    @Override
    @Transactional
    public void handleWebhookEvent(PaymentMethod provider, Map<String, String> headers, String payload) {
        PaymentGateway gateway = paymentGatewayResolver.resolve(provider);
        WebhookResult result = gateway.processWebhook(headers, payload);

        if (!result.successful()) {
            log.warn("Webhook processing was skipped or unsuccessful for provider {}: {}", provider, result.failureReason());
            return;
        }

        Optional<Payment> paymentOpt = findPaymentForWebhook(result);
        if (paymentOpt.isEmpty()) {
            log.warn("Payment not found for webhook transaction: {} (orderId: {})",
                    result.gatewayTransactionId(), result.orderId());
            return;
        }

        Payment payment = paymentOpt.get();

        if (payment.getStatus() == PaymentStatus.SUCCEEDED && result.paymentStatus() == PaymentStatus.SUCCEEDED) {
            log.info("Payment with ID {} is already marked as SUCCEEDED. Skipping duplicate webhook.", payment.getId());
            return;
        }

        if (result.paymentStatus() == PaymentStatus.SUCCEEDED) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            if (result.gatewayTransactionId() != null) {
                payment.setGatewayTransactionId(result.gatewayTransactionId());
            }
            paymentRepository.save(payment);

            paymentLedgerService.recordCapture(payment, result.gatewayTransactionId(), payload);

            log.info("Payment {} successfully marked as SUCCEEDED for customer {}", payment.getId(), payment.getCustomerId());

            eventPublisher.publishEvent(new PaymentCompletedEvent(
                    payment.getId(),
                    payment.getCustomerId(),
                    payment.getOrderId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getGatewayTransactionId()
            ));
        } else if (result.paymentStatus() == PaymentStatus.FAILED) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.failureReason());
            paymentRepository.save(payment);

            paymentLedgerService.recordAdjustment(
                    payment,
                    payment.getAmount(),
                    BalanceType.CREDIT,
                    "Webhook marked payment as FAILED: " + result.failureReason()
            );

            log.warn("Payment {} marked as FAILED. Reason: {}", payment.getId(), payment.getFailureReason());
        } else if (result.paymentStatus() == PaymentStatus.CANCELLED) {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.CANCELLED);
                payment.setFailureReason(result.failureReason());
                paymentRepository.save(payment);

                paymentLedgerService.recordAdjustment(
                        payment,
                        payment.getAmount(),
                        BalanceType.CREDIT,
                        "Webhook marked payment as CANCELLED"
                );

                log.info("Payment {} marked as CANCELLED due to expired/cancelled session", payment.getId());
            }
        }
    }

    @Override
    @Transactional
    public RefundResult refundPayment(AuthUser customer, UUID paymentId, RefundPaymentRequest request, String idempotencyKey) {
        Payment payment = paymentRepository.findByIdAndCustomerId(paymentId, customer.id())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_NOT_FOUND, paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCEEDED && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new ApplicationException(ErrorCode.PAYMENT_GATEWAY_ERROR, "Cannot refund payment in status: " + payment.getStatus());
        }

        BigDecimal newTotalRefunded = payment.getRefundedAmount().add(request.amount());
        if (newTotalRefunded.compareTo(payment.getAmount()) > 0) {
            throw new ApplicationException(
                    ErrorCode.REFUND_AMOUNT_EXCEEDS_PAYMENT,
                    request.amount(),
                    payment.getRefundableAmount()
            );
        }

        PaymentGateway gateway = paymentGatewayResolver.resolve(payment.getMethod());
        RefundResult refundResult = gateway.refund(payment, request.amount(), request.reason(), idempotencyKey);

        if (!refundResult.successful()) {
            throw new ApplicationException(ErrorCode.PAYMENT_GATEWAY_ERROR, refundResult.failureReason());
        }

        payment.setRefundedAmount(newTotalRefunded);
        if (newTotalRefunded.compareTo(payment.getAmount()) == 0) {
            payment.setStatus(PaymentStatus.REFUNDED);
        } else {
            payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
        paymentRepository.save(payment);

        paymentLedgerService.recordRefund(
                payment,
                request.amount(),
                refundResult.gatewayRefundId(),
                request.reason()
        );

        log.info("Successfully processed refund for payment ID {}: amount={}, remainingRefundable={}",
                paymentId, request.amount(), payment.getRefundableAmount());

        return refundResult;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(AuthUser customer, UUID paymentId) {
        Payment payment = paymentRepository.findByIdAndCustomerId(paymentId, customer.id())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_NOT_FOUND, paymentId));
        return PaymentResponse.from(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getCustomerPayments(AuthUser customer) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customer.id())
                .stream()
                .map(PaymentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentLedgerResponse> getPaymentLedger(AuthUser customer, UUID paymentId) {
        paymentRepository.findByIdAndCustomerId(paymentId, customer.id())
                .orElseThrow(() -> new ApplicationException(ErrorCode.PAYMENT_NOT_FOUND, paymentId));

        return paymentLedgerService.getLedgerByPaymentId(paymentId)
                .stream()
                .map(PaymentLedgerResponse::from)
                .toList();
    }

    private Optional<Payment> findPaymentForWebhook(WebhookResult result) {
        if (result.gatewayTransactionId() != null) {
            Optional<Payment> byTxId = paymentRepository.findByGatewayTransactionId(result.gatewayTransactionId());
            if (byTxId.isPresent()) {
                return byTxId;
            }
            Optional<Payment> bySessionId = paymentRepository.findByGatewaySessionId(result.gatewayTransactionId());
            if (bySessionId.isPresent()) {
                return bySessionId;
            }
        }

        if (result.orderId() != null) {
            return paymentRepository.findByOrderId(result.orderId());
        }

        return Optional.empty();
    }
}
