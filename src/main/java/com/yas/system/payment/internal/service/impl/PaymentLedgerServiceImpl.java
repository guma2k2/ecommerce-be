package com.yas.system.payment.internal.service.impl;

import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.entity.PaymentLedger;
import com.yas.system.payment.internal.enumeration.BalanceType;
import com.yas.system.payment.internal.enumeration.LedgerEntryType;
import com.yas.system.payment.internal.repository.PaymentLedgerRepository;
import com.yas.system.payment.internal.service.PaymentLedgerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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
public class PaymentLedgerServiceImpl implements PaymentLedgerService {

    PaymentLedgerRepository paymentLedgerRepository;

    @Override
    @Transactional
    public PaymentLedger recordInitialization(Payment payment) {
        String txId = payment.getGatewaySessionId() != null
                ? payment.getGatewaySessionId()
                : "init_" + payment.getId();

        return saveLedgerEntry(
                payment,
                LedgerEntryType.INITIALIZATION,
                BalanceType.DEBIT,
                payment.getAmount(),
                txId,
                "Initialized checkout session"
        );
    }

    @Override
    @Transactional
    public PaymentLedger recordCapture(Payment payment, String gatewayTxId, String rawPayload) {
        String txId = gatewayTxId != null ? gatewayTxId : "capture_" + payment.getId();

        return saveLedgerEntry(
                payment,
                LedgerEntryType.CAPTURE,
                BalanceType.DEBIT,
                payment.getAmount(),
                txId,
                rawPayload != null && rawPayload.length() > 500 ? rawPayload.substring(0, 500) : rawPayload
        );
    }

    @Override
    @Transactional
    public PaymentLedger recordRefund(Payment payment, BigDecimal refundAmount, String gatewayRefundId, String reason) {
        String txId = gatewayRefundId != null ? gatewayRefundId : "ref_" + UUID.randomUUID().toString().substring(0, 8);

        return saveLedgerEntry(
                payment,
                LedgerEntryType.REFUND,
                BalanceType.CREDIT,
                refundAmount,
                txId,
                reason
        );
    }

    @Override
    @Transactional
    public PaymentLedger recordAdjustment(Payment payment, BigDecimal amount, BalanceType balanceType, String reason) {
        String txId = "adj_" + UUID.randomUUID().toString().substring(0, 8);

        return saveLedgerEntry(
                payment,
                LedgerEntryType.ADJUSTMENT,
                balanceType,
                amount,
                txId,
                reason
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentLedger> getLedgerByPaymentId(UUID paymentId) {
        return paymentLedgerRepository.findByPaymentIdOrderByCreatedAtAsc(paymentId);
    }


    private PaymentLedger saveLedgerEntry(
            Payment payment,
            LedgerEntryType entryType,
            BalanceType balanceType,
            BigDecimal amount,
            String gatewayTxId,
            String metadata
    ) {
        Optional<PaymentLedger> existing = paymentLedgerRepository
                .findByGatewayProviderAndGatewayTransactionId(payment.getMethod(), gatewayTxId);
        if (existing.isPresent()) {
            log.info("Ledger entry already exists for provider {} and txId: {}. Skipping duplicate write.",
                    payment.getMethod(), gatewayTxId);
            return existing.get();
        }

        PaymentLedger ledger = PaymentLedger.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrderId())
                .entryType(entryType)
                .balanceType(balanceType)
                .amount(amount)
                .currency(payment.getCurrency())
                .gatewayProvider(payment.getMethod())
                .gatewayTransactionId(gatewayTxId)
                .idempotencyKey(payment.getIdempotencyKey())
                .metadata(metadata)
                .build();

        try {
            PaymentLedger saved = paymentLedgerRepository.save(ledger);
            log.info("Recorded immutable ledger entry: id={}, type={}, amount={} {}, paymentId={}, txId={}",
                    saved.getId(), entryType, amount, payment.getCurrency(), payment.getId(), gatewayTxId);
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.warn("Concurrent duplicate ledger write caught for provider {} and txId: {}", payment.getMethod(), gatewayTxId);
            return paymentLedgerRepository
                    .findByGatewayProviderAndGatewayTransactionId(payment.getMethod(), gatewayTxId)
                    .orElseThrow(() -> e);
        }
    }
}
