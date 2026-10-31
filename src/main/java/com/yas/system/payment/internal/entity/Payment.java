package com.yas.system.payment.internal.entity;

import com.yas.system.common.entity.BaseUuidEntity;
import com.yas.system.payment.enumeration.PaymentMethod;
import com.yas.system.payment.enumeration.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "tbl_payment",
        indexes = {
                @Index(name = "idx_payment_customer", columnList = "customer_id"),
                @Index(name = "idx_payment_order", columnList = "order_id"),
                @Index(name = "idx_payment_session", columnList = "gateway_session_id"),
                @Index(name = "idx_payment_transaction", columnList = "gateway_transaction_id"),
                @Index(name = "idx_payment_idempotency", columnList = "idempotency_key", unique = true)
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment extends BaseUuidEntity {

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "order_id", length = 36)
    private String orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Builder.Default
    @Column(name = "refunded_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 30)
    private PaymentMethod method;

    @Column(name = "gateway_session_id", length = 100)
    private String gatewaySessionId;

    @Column(name = "gateway_transaction_id", length = 100)
    private String gatewayTransactionId;

    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    public BigDecimal getRefundableAmount() {
        return amount.subtract(refundedAmount != null ? refundedAmount : BigDecimal.ZERO);
    }
}
