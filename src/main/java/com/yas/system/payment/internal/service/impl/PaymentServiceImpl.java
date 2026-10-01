package com.yas.system.payment.internal.service.impl;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.dto.request.CreateCheckoutSessionRequest;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.dto.response.PaymentResponse;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.event.PaymentCompletedEvent;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.repository.PaymentRepository;
import com.yas.system.payment.internal.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentServiceImpl implements PaymentService {

    PaymentRepository paymentRepository;
    PaymentGateway paymentGateway;
    ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public CheckoutSessionResponse createCheckoutSession(AuthUser customer, CreateCheckoutSessionRequest request) {
        Payment payment = Payment.builder()
                .customerId(customer.id())
                .orderId(request.orderId())
                .amount(request.amount())
                .currency(request.currency().toUpperCase())
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.STRIPE)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        CheckoutSessionResponse sessionResponse = paymentGateway.createCheckoutSession(savedPayment, customer.email());

        savedPayment.setStripeSessionId(sessionResponse.sessionId());
        paymentRepository.save(savedPayment);

        return sessionResponse;
    }

    @Override
    @Transactional
    public void handleWebhookEvent(String payload, String signatureHeader) {
        Event event = paymentGateway.constructWebhookEvent(payload, signatureHeader);
        String eventType = event.getType();
        log.info("Processing Stripe webhook event: id={}, type={}", event.getId(), eventType);

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);
        if (stripeObject == null) {
            try {
                stripeObject = dataObjectDeserializer.deserializeUnsafe();
            } catch (Exception e) {
                log.warn("deserializeUnsafe failed for event {}: {}", event.getId(), e.getMessage());
            }
        }

        switch (eventType) {
            case "checkout.session.completed" -> {
                if (stripeObject instanceof Session session) {
                    handleCheckoutSessionCompleted(session);
                } else {
                    log.warn("Unable to deserialize checkout session for event: {}", event.getId());
                }
            }
            case "payment_intent.payment_failed" -> {
                if (stripeObject instanceof PaymentIntent paymentIntent) {
                    handlePaymentIntentFailed(paymentIntent);
                } else {
                    log.warn("Unable to deserialize payment intent for event: {}", event.getId());
                }
            }
            case "checkout.session.expired" -> {
                if (stripeObject instanceof Session session) {
                    handleCheckoutSessionExpired(session);
                } else {
                    log.warn("Unable to deserialize expired checkout session for event: {}", event.getId());
                }
            }
            default -> log.debug("Unhandled Stripe event type: {}", eventType);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(AuthUser customer, Long paymentId) {
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

    private void handleCheckoutSessionCompleted(Session session) {
        Optional<Payment> paymentOpt = findPaymentFromSession(session);
        if (paymentOpt.isEmpty()) {
            log.warn("Payment not found for completed Stripe session: {}", session.getId());
            return;
        }

        Payment payment = paymentOpt.get();
        if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
            log.info("Payment with ID {} is already marked as SUCCEEDED. Skipping.", payment.getId());
            return;
        }

        payment.setStatus(PaymentStatus.SUCCEEDED);
        payment.setStripePaymentIntentId(session.getPaymentIntent());
        paymentRepository.save(payment);

        log.info("Payment {} successfully marked as SUCCEEDED for customer {}", payment.getId(), payment.getCustomerId());

        eventPublisher.publishEvent(new PaymentCompletedEvent(
                payment.getId(),
                payment.getCustomerId(),
                payment.getOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStripePaymentIntentId()
        ));
    }

    private void handlePaymentIntentFailed(PaymentIntent paymentIntent) {
        Optional<Payment> paymentOpt = paymentRepository.findByStripePaymentIntentId(paymentIntent.getId());
        if (paymentOpt.isEmpty() && paymentIntent.getMetadata() != null) {
            String paymentIdStr = paymentIntent.getMetadata().get("payment_id");
            if (paymentIdStr != null) {
                try {
                    paymentOpt = paymentRepository.findById(Long.parseLong(paymentIdStr));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        paymentOpt.ifPresent(payment -> {
            payment.setStatus(PaymentStatus.FAILED);
            if (paymentIntent.getLastPaymentError() != null) {
                payment.setFailureReason(paymentIntent.getLastPaymentError().getMessage());
            }
            paymentRepository.save(payment);
            log.warn("Payment {} marked as FAILED. Reason: {}", payment.getId(), payment.getFailureReason());
        });
    }

    private void handleCheckoutSessionExpired(Session session) {
        findPaymentFromSession(session).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.CANCELLED);
                paymentRepository.save(payment);
                log.info("Payment {} marked as CANCELLED due to expired session", payment.getId());
            }
        });
    }

    private Optional<Payment> findPaymentFromSession(Session session) {
        Optional<Payment> bySessionId = paymentRepository.findByStripeSessionId(session.getId());
        if (bySessionId.isPresent()) {
            return bySessionId;
        }

        if (session.getMetadata() != null) {
            String paymentIdStr = session.getMetadata().get("payment_id");
            if (paymentIdStr != null) {
                try {
                    return paymentRepository.findById(Long.parseLong(paymentIdStr));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return Optional.empty();
    }
}
