package com.yas.system.payment;

import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.constant.PaymentConstant;
import com.yas.system.payment.internal.dto.request.CreateCheckoutSessionRequest;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.dto.response.PaymentResponse;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.event.PaymentCompletedEvent;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.repository.PaymentRepository;
import com.yas.system.payment.internal.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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
    ApplicationEventPublisher eventPublisher;

    PaymentServiceImpl paymentService;

    AuthUser testUser;
    String customerId = "customer-uuid-123";

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentRepository, paymentGateway, eventPublisher);
        testUser = new AuthUser(customerId, "customer@example.com", "ROLE_USER", "password", List.of());
    }

    @Test
    @DisplayName("createCheckoutSession should save payment and return checkout url")
    void createCheckoutSession_success() {
        CreateCheckoutSessionRequest request = new CreateCheckoutSessionRequest(
                "10",
                BigDecimal.valueOf(99.99),
                "USD"
        );

        Payment savedPayment = Payment.builder()
                .customerId(customerId)
                .orderId("10")
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.STRIPE)
                .build();
        savedPayment.setId(1L);

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentGateway.createCheckoutSession(eq(savedPayment), eq("customer@example.com"), any()))
                .thenReturn(new CheckoutSessionResponse(1L, "cs_test_123", "https://checkout.stripe.com/pay/cs_test_123"));

        CheckoutSessionResponse response = paymentService.createCheckoutSession(testUser, request);

        assertThat(response).isNotNull();
        assertThat(response.paymentId()).isEqualTo(1L);
        assertThat(response.sessionId()).isEqualTo("cs_test_123");
        assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe.com/pay/cs_test_123");

        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    @DisplayName("handleWebhookEvent should mark payment as SUCCEEDED and publish event")
    void handleWebhookEvent_completed() {
        String payload = "{}";
        String sig = "sig_header";

        Event event = mock(Event.class);
        when(event.getId()).thenReturn("evt_123");
        when(event.getType()).thenReturn(PaymentConstant.WebhookTopic.CHECKOUT_SESSION_COMPLETED);

        Session session = mock(Session.class);
        when(session.getId()).thenReturn("cs_test_123");
        when(session.getPaymentIntent()).thenReturn("pi_123");

        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);

        Payment payment = Payment.builder()
                .customerId(customerId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.STRIPE)
                .stripeSessionId("cs_test_123")
                .build();
        payment.setId(1L);

        when(paymentGateway.constructWebhookEvent(payload, sig)).thenReturn(event);
        when(paymentRepository.findByStripeSessionId("cs_test_123")).thenReturn(Optional.of(payment));

        paymentService.handleWebhookEvent(payload, sig);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(payment.getStripePaymentIntentId()).isEqualTo("pi_123");

        verify(paymentRepository).save(payment);
        verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("handleWebhookEvent should be idempotent if payment already SUCCEEDED")
    void handleWebhookEvent_idempotent() {
        String payload = "{}";
        String sig = "sig_header";

        Event event = mock(Event.class);
        when(event.getId()).thenReturn("evt_123");
        when(event.getType()).thenReturn(PaymentConstant.WebhookTopic.CHECKOUT_SESSION_COMPLETED);

        Session session = mock(Session.class);
        when(session.getId()).thenReturn("cs_test_123");

        EventDataObjectDeserializer deserializer = mock(EventDataObjectDeserializer.class);
        when(deserializer.getObject()).thenReturn(Optional.of(session));
        when(event.getDataObjectDeserializer()).thenReturn(deserializer);

        Payment payment = Payment.builder()
                .customerId(customerId)
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(PaymentStatus.SUCCEEDED)
                .method(PaymentMethod.STRIPE)
                .stripeSessionId("cs_test_123")
                .build();
        payment.setId(1L);

        when(paymentGateway.constructWebhookEvent(payload, sig)).thenReturn(event);
        when(paymentRepository.findByStripeSessionId("cs_test_123")).thenReturn(Optional.of(payment));

        paymentService.handleWebhookEvent(payload, sig);

        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("getPaymentById should return payment when found")
    void getPaymentById_found() {
        Payment payment = Payment.builder()
                .customerId(customerId)
                .orderId("10")
                .amount(BigDecimal.valueOf(99.99))
                .currency("USD")
                .status(PaymentStatus.SUCCEEDED)
                .method(PaymentMethod.STRIPE)
                .stripeSessionId("cs_test_123")
                .build();
        payment.setId(1L);

        when(paymentRepository.findByIdAndCustomerId(1L, customerId)).thenReturn(Optional.of(payment));

        PaymentResponse response = paymentService.getPaymentById(testUser, 1L);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.status()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(99.99));
    }

    @Test
    @DisplayName("getPaymentById should throw PAYMENT_NOT_FOUND when not found")
    void getPaymentById_notFound() {
        when(paymentRepository.findByIdAndCustomerId(999L, customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(testUser, 999L))
                .isInstanceOf(ApplicationException.class)
                .hasMessageContaining("Payment with ID 999 not found");
    }
}
