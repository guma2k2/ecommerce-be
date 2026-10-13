package com.yas.system.payment.internal.entity;

import com.yas.system.common.entity.BaseUuidEntity;
import com.yas.system.payment.internal.enumeration.BalanceType;
import com.yas.system.payment.internal.enumeration.LedgerEntryType;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "tbl_payment_ledger",
        indexes = {
                @Index(name = "idx_ledger_payment", columnList = "payment_id"),
                @Index(name = "idx_ledger_order", columnList = "order_id"),
                @Index(name = "idx_ledger_provider_tx", columnList = "gateway_provider, gateway_transaction_id", unique = true)
        }
)
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentLedger extends BaseUuidEntity {

    @Column(name = "payment_id", nullable = false)
    private UUID paymentId;

    @Column(name = "order_id", length = 36)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 30)
    private LedgerEntryType entryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "balance_type", nullable = false, length = 10)
    private BalanceType balanceType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway_provider", nullable = false, length = 30)
    private PaymentMethod gatewayProvider;

    @Column(name = "gateway_transaction_id", nullable = false, length = 100)
    private String gatewayTransactionId;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
}
