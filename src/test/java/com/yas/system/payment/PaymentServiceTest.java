package com.yas.system.payment;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.dto.request.CreateCheckoutSessionRequest;
import com.yas.system.payment.internal.dto.request.RefundPaymentRequest;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.dto.response.PaymentResponse;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.event.PaymentCompletedEvent;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.gateway.PaymentGatewayResolver;
import com.yas.system.payment.internal.gateway.dto.RefundResult;
import com.yas.system.payment.internal.gateway.dto.WebhookResult;
import com.yas.system.payment.internal.repository.PaymentRepository;
import com.yas.system.payment.internal.service.PaymentIdempotencyService;
import com.yas.system.payment.internal.service.PaymentLedgerService;
import com.yas.system.payment.internal.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PaymentGateway paymentGateway;

    @Mock
    PaymentGatewayResolver paymentGatewayResolver;

    @Mock
    PaymentIdempotencyService paymentIdempotencyService;

    @Mock
    PaymentLedgerService paymentLedgerService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    PaymentServiceImpl paymentService;

    AuthUser testUser;
    String customerId = "customer-uuid-123";

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(
                paymentRepository,
                paymentGatewayResolver,
                paymentIdempotencyService,
                paymentLedgerService,
                eventPublisher
        );
        testUser = new AuthUser(customerId, "customer@example.com", "ROLE_USER", "password", List.of());
    }

    @Test
    @DisplayName("createCheckoutSession should save payment, record in ledger and save in idempotency cache")
    void createCheckoutSession_success() {
        UUID paymentId = UUID.randomUUID();
        CreateCheckoutSessionRequest request = new CreateCheckoutSessionRequest(
                "order-10",
                BigDecimal.valueOf(99.99),
                "USD",
                PaymentMethod.STRIPE.name(),
                "http://localhost:3000/success",
                "http://localhost:3000/cancel"
        );

        Payment savedPayment = Payment.builder()
                .customerId(customerId)
                .orderId("order-10")
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.STRIPE)
                .build();
        savedPayment.setId(paymentId);

        when(paymentIdempotencyService.checkAndLock("idemp-key-1")).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentGatewayResolver.resolve(PaymentMethod.STRIPE)).thenReturn(paymentGateway);
        when(paymentGateway.createCheckoutSession(eq(savedPayment), any()))
                .thenReturn(new CheckoutSessionResponse(paymentId, "cs_test_123", "https://checkout.stripe.com/pay/cs_test_123"));

        CheckoutSessionResponse response = paymentService.createCheckoutSession(testUser, "idemp-key-1", request);

        assertThat(response).isNotNull();
        assertThat(response.paymentId()).isEqualTo(paymentId);
        assertThat(response.sessionId()).isEqualTo("cs_test_123");
        assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe.com/pay/cs_test_123");

        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(paymentLedgerService).recordInitialization(any(Payment.class));
        verify(paymentIdempotencyService).saveCompleted(eq("idemp-key-1"), eq(response));
    }

    @Test
    @DisplayName("createCheckoutSession should return cached response when idempotency hit")
    void createCheckoutSession_cachedHit() {
        UUID paymentId = UUID.randomUUID();
        CreateCheckoutSessionRequest request = new CreateCheckoutSessionRequest(
                "order-10",
                BigDecimal.valueOf(99.99),
                "USD",
                PaymentMethod.STRIPE.name(),
                null,
                null
        );

        CheckoutSessionResponse cachedResponse = new CheckoutSessionResponse(
                paymentId,
                "cs_cached_123",
                "https://checkout.stripe.com/pay/cs_cached_123"
        );

        when(paymentIdempotencyService.checkAndLock("idemp-key-cached")).thenReturn(Optional.of(cachedResponse));

        CheckoutSessionResponse response = paymentService.createCheckoutSession(testUser, "idemp-key-cached", request);

        assertThat(response).isEqualTo(cachedResponse);
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentGatewayResolver, never()).resolve(any());
    }

    @Test
    @DisplayName("handleWebhookEvent should mark payment as SUCCEEDED, record capture in ledger, and publish event")
    void handleWebhookEvent_completed() {
        String payload = "{}";
        Map<String, String> headers = Map.of("Stripe-Signature", "sig_123");
        UUID paymentId = UUID.randomUUID();

        WebhookResult webhookResult = new WebhookResult(
                true,
                "evt_123",
                "pi_123",
                "order-10",
                PaymentStatus.SUCCEEDED,
                null,
                payload
        );

        Payment payment = Payment.builder()
                .customerId(customerId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.STRIPE)
                .gatewaySessionId("cs_test_123")
                .build();
        payment.setId(paymentId);

        when(paymentGatewayResolver.resolve(PaymentMethod.STRIPE)).thenReturn(paymentGateway);
        when(paymentGateway.processWebhook(headers, payload)).thenReturn(webhookResult);
        when(paymentRepository.findByGatewayTransactionId("pi_123")).thenReturn(Optional.of(payment));

        paymentService.handleWebhookEvent(PaymentMethod.STRIPE, headers, payload);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(payment.getGatewayTransactionId()).isEqualTo("pi_123");

        verify(paymentRepository).save(payment);
        verify(paymentLedgerService).recordCapture(eq(payment), eq("pi_123"), eq(payload));
        verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("handleWebhookEvent should be idempotent if payment already SUCCEEDED")
    void handleWebhookEvent_idempotent() {
        String payload = "{}";
        Map<String, String> headers = Map.of("Stripe-Signature", "sig_123");
        UUID paymentId = UUID.randomUUID();

        WebhookResult webhookResult = new WebhookResult(
                true,
                "evt_123",
                "pi_123",
                "order-10",
                PaymentStatus.SUCCEEDED,
                null,
                payload
        );

        Payment payment = Payment.builder()
                .customerId(customerId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(PaymentStatus.SUCCEEDED)
                .method(PaymentMethod.STRIPE)
                .gatewayTransactionId("pi_123")
                .build();
        payment.setId(paymentId);

        when(paymentGatewayResolver.resolve(PaymentMethod.STRIPE)).thenReturn(paymentGateway);
        when(paymentGateway.processWebhook(headers, payload)).thenReturn(webhookResult);
        when(paymentRepository.findByGatewayTransactionId("pi_123")).thenReturn(Optional.of(payment));

        paymentService.handleWebhookEvent(PaymentMethod.STRIPE, headers, payload);

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(paymentLedgerService, never()).recordCapture(any(), any(), any());
        verify(eventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("refundPayment should support partial refunds, update payment status to PARTIALLY_REFUNDED and append to ledger")
    void refundPayment_partialSuccess() {
        UUID paymentId = UUID.randomUUID();
        RefundPaymentRequest refundRequest = new RefundPaymentRequest(BigDecimal.valueOf(30.00), "Damaged item returned");

        Payment payment = Payment.builder()
                .customerId(customerId)
                .amount(BigDecimal.valueOf(100.00))
                .refundedAmount(BigDecimal.ZERO)
                .currency("USD")
                .status(PaymentStatus.SUCCEEDED)
                .method(PaymentMethod.STRIPE)
                .gatewayTransactionId("pi_123")
                .build();
        payment.setId(paymentId);

        when(paymentRepository.findByIdAndCustomerId(paymentId, customerId)).thenReturn(Optional.of(payment));
        when(paymentGatewayResolver.resolve(PaymentMethod.STRIPE)).thenReturn(paymentGateway);
        when(paymentGateway.refund(eq(payment), eq(BigDecimal.valueOf(30.00)), eq("Damaged item returned"), eq("ref-key-1")))
                .thenReturn(new RefundResult(true, "re_123", BigDecimal.valueOf(30.00), null));

        RefundResult result = paymentService.refundPayment(testUser, paymentId, refundRequest, "ref-key-1");

        assertThat(result.successful()).isTrue();
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PARTIALLY_REFUNDED);
        assertThat(payment.getRefundedAmount()).isEqualByComparingTo(BigDecimal.valueOf(30.00));

        verify(paymentRepository).save(payment);
        verify(paymentLedgerService).recordRefund(eq(payment), eq(BigDecimal.valueOf(30.00)), eq("re_123"), eq("Damaged item returned"));
    }

    @Test
    @DisplayName("getPaymentById should return payment when found")
    void getPaymentById_found() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .customerId(customerId)
                .orderId("10")
                .amount(BigDecimal.valueOf(99.99))
                .refundedAmount(BigDecimal.ZERO)
                .currency("USD")
                .status(PaymentStatus.SUCCEEDED)
                .method(PaymentMethod.STRIPE)
                .gatewaySessionId("cs_test_123")
                .build();
        payment.setId(paymentId);

        when(paymentRepository.findByIdAndCustomerId(paymentId, customerId)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(testUser, paymentId);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(paymentId);
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    }

    @Test
    @DisplayName("getPaymentById should throw PAYMENT_NOT_FOUND when not found")
    void getPaymentById_notFound() {
        UUID paymentId = UUID.randomUUID();
        when(paymentRepository.findByIdAndCustomerId(paymentId, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(testUser, paymentId))
                .isInstanceOf(ApplicationException.class);
    }
}
