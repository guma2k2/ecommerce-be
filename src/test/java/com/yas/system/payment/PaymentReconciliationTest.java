package com.yas.system.payment;

import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.BalanceType;
import com.yas.system.payment.internal.enumeration.LedgerEntryType;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.event.PaymentCompletedEvent;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.gateway.PaymentGatewayResolver;
import com.yas.system.payment.internal.gateway.dto.PaymentStatusResult;
import com.yas.system.payment.internal.repository.PaymentRepository;
import com.yas.system.payment.internal.service.PaymentLedgerService;
import com.yas.system.payment.internal.service.impl.PaymentReconciliationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentReconciliationTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    PaymentGatewayResolver paymentGatewayResolver;

    @Mock
    PaymentGateway paymentGateway;

    @Mock
    PaymentLedgerService paymentLedgerService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    PaymentReconciliationServiceImpl reconciliationService;

    @BeforeEach
    void setUp() {
        reconciliationService = new PaymentReconciliationServiceImpl(
                paymentRepository,
                paymentGatewayResolver,
                paymentLedgerService,
                eventPublisher
        );
    }

    @Test
    @DisplayName("reconcilePendingPayments should sync paid transactions to SUCCEEDED, record in ledger and publish event")
    void reconcilePendingPayments_success() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .customerId("customer-123")
                .orderId("order-456")
                .amount(BigDecimal.valueOf(50.00))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.STRIPE)
                .gatewaySessionId("cs_stale_123")
                .build();
        payment.setId(paymentId);

        when(paymentRepository.findByStatusAndCreatedAtBeforeAndCreatedAtAfter(eq(PaymentStatus.PENDING), any(), any()))
                .thenReturn(List.of(payment));
        when(paymentGatewayResolver.resolve(PaymentMethod.STRIPE)).thenReturn(paymentGateway);
        when(paymentGateway.queryTransactionStatus(payment))
                .thenReturn(new PaymentStatusResult(PaymentStatus.SUCCEEDED, "pi_reconciled_999", BigDecimal.valueOf(50.00), "USD", null, "paid"));

        reconciliationService.reconcilePendingPayments();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(payment.getGatewayTransactionId()).isEqualTo("pi_reconciled_999");

        verify(paymentRepository).save(payment);
        verify(paymentLedgerService).recordCapture(eq(payment), eq("pi_reconciled_999"), anyString());
        verify(eventPublisher).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("reconcilePendingPayments should mark failed/cancelled transactions in DB and ledger")
    void reconcilePendingPayments_cancelled() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .customerId("customer-123")
                .orderId("order-456")
                .amount(BigDecimal.valueOf(50.00))
                .currency("USD")
                .status(PaymentStatus.PENDING)
                .method(PaymentMethod.STRIPE)
                .gatewaySessionId("cs_expired_123")
                .build();
        payment.setId(paymentId);

        when(paymentRepository.findByStatusAndCreatedAtBeforeAndCreatedAtAfter(eq(PaymentStatus.PENDING), any(), any()))
                .thenReturn(List.of(payment));
        when(paymentGatewayResolver.resolve(PaymentMethod.STRIPE)).thenReturn(paymentGateway);
        when(paymentGateway.queryTransactionStatus(payment))
                .thenReturn(new PaymentStatusResult(PaymentStatus.CANCELLED, "cs_expired_123", BigDecimal.valueOf(50.00), "USD", "Session expired", "expired"));

        reconciliationService.reconcilePendingPayments();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getFailureReason()).isEqualTo("Session expired");

        verify(paymentRepository).save(payment);
        verify(paymentLedgerService).recordAdjustment(eq(payment), eq(BigDecimal.valueOf(50.00)), eq(BalanceType.CREDIT), anyString());
        verify(eventPublisher, never()).publishEvent(any(PaymentCompletedEvent.class));
    }
}
