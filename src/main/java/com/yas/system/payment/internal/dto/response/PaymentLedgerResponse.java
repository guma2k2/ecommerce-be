package com.yas.system.payment.internal.dto.response;

import com.yas.system.common.util.DateTimeUtils;
import com.yas.system.payment.internal.entity.PaymentLedger;
import com.yas.system.payment.internal.enumeration.BalanceType;
import com.yas.system.payment.internal.enumeration.LedgerEntryType;
import com.yas.system.payment.internal.enumeration.PaymentMethod;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentLedgerResponse(
        UUID id,
        UUID paymentId,
        String orderId,
        LedgerEntryType entryType,
        BalanceType balanceType,
        BigDecimal amount,
        String currency,
        PaymentMethod gatewayProvider,
        String gatewayTransactionId,
        String idempotencyKey,
        String metadata,
        String createdAt
) {
    public static PaymentLedgerResponse from(PaymentLedger ledger) {
        return new PaymentLedgerResponse(
                ledger.getId(),
                ledger.getPaymentId(),
                ledger.getOrderId(),
                ledger.getEntryType(),
                ledger.getBalanceType(),
                ledger.getAmount(),
                ledger.getCurrency(),
                ledger.getGatewayProvider(),
                ledger.getGatewayTransactionId(),
                ledger.getIdempotencyKey(),
                ledger.getMetadata(),
                DateTimeUtils.format(ledger.getCreatedAt())
        );
    }
}
