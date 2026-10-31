package com.yas.system.payment;

import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.entity.PaymentLedger;
import com.yas.system.payment.enumeration.BalanceType;
import com.yas.system.payment.enumeration.LedgerEntryType;
import com.yas.system.payment.enumeration.PaymentMethod;
import com.yas.system.payment.enumeration.PaymentStatus;
import com.yas.system.payment.internal.repository.PaymentLedgerRepository;
import com.yas.system.payment.internal.service.impl.PaymentLedgerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentLedgerServiceTest {

    @Mock
    PaymentLedgerRepository paymentLedgerRepository;

    PaymentLedgerServiceImpl paymentLedgerService;

    @BeforeEach
    void setUp() {
        paymentLedgerService = new PaymentLedgerServiceImpl(paymentLedgerRepository);
    }

    @Test
    @DisplayName("recordCapture should save CAPTURE ledger entry with DEBIT balance")
    void recordCapture_success() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .customerId("cust-1")
                .orderId("ord-1")
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .status(PaymentStatus.SUCCEEDED)
                .method(PaymentMethod.STRIPE)
                .build();
        payment.setId(paymentId);

        when(paymentLedgerRepository.findByGatewayProviderAndGatewayTransactionId(PaymentMethod.STRIPE, "pi_100"))
                .thenReturn(Optional.empty());
        when(paymentLedgerRepository.save(any(PaymentLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentLedger ledger = paymentLedgerService.recordCapture(payment, "pi_100", "{\"status\":\"succeeded\"}");

        assertThat(ledger).isNotNull();
        assertThat(ledger.getPaymentId()).isEqualTo(paymentId);
        assertThat(ledger.getEntryType()).isEqualTo(LedgerEntryType.CAPTURE);
        assertThat(ledger.getBalanceType()).isEqualTo(BalanceType.DEBIT);
        assertThat(ledger.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(ledger.getGatewayTransactionId()).isEqualTo("pi_100");

        verify(paymentLedgerRepository).save(any(PaymentLedger.class));
    }

    @Test
    @DisplayName("recordRefund should save REFUND ledger entry with CREDIT balance")
    void recordRefund_success() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .customerId("cust-1")
                .orderId("ord-1")
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .status(PaymentStatus.PARTIALLY_REFUNDED)
                .method(PaymentMethod.STRIPE)
                .build();
        payment.setId(paymentId);

        when(paymentLedgerRepository.findByGatewayProviderAndGatewayTransactionId(PaymentMethod.STRIPE, "re_200"))
                .thenReturn(Optional.empty());
        when(paymentLedgerRepository.save(any(PaymentLedger.class))).thenAnswer(inv -> inv.getArgument(0));

        PaymentLedger ledger = paymentLedgerService.recordRefund(payment, BigDecimal.valueOf(30.00), "re_200", "Item returned");

        assertThat(ledger).isNotNull();
        assertThat(ledger.getEntryType()).isEqualTo(LedgerEntryType.REFUND);
        assertThat(ledger.getBalanceType()).isEqualTo(BalanceType.CREDIT);
        assertThat(ledger.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(30.00));
        assertThat(ledger.getGatewayTransactionId()).isEqualTo("re_200");

        verify(paymentLedgerRepository).save(any(PaymentLedger.class));
    }

    @Test
    @DisplayName("recordCapture should skip insert when entry with same gateway transaction id already exists")
    void recordCapture_idempotentDuplicate() {
        UUID paymentId = UUID.randomUUID();
        Payment payment = Payment.builder()
                .customerId("cust-1")
                .orderId("ord-1")
                .amount(BigDecimal.valueOf(100.00))
                .currency("USD")
                .status(PaymentStatus.SUCCEEDED)
                .method(PaymentMethod.STRIPE)
                .build();
        payment.setId(paymentId);

        PaymentLedger existingLedger = PaymentLedger.builder()
                .paymentId(paymentId)
                .gatewayProvider(PaymentMethod.STRIPE)
                .gatewayTransactionId("pi_100")
                .entryType(LedgerEntryType.CAPTURE)
                .amount(BigDecimal.valueOf(100.00))
                .build();

        when(paymentLedgerRepository.findByGatewayProviderAndGatewayTransactionId(PaymentMethod.STRIPE, "pi_100"))
                .thenReturn(Optional.of(existingLedger));

        PaymentLedger result = paymentLedgerService.recordCapture(payment, "pi_100", "duplicate payload");

        assertThat(result).isSameAs(existingLedger);
        verify(paymentLedgerRepository, never()).save(any(PaymentLedger.class));
    }
}
