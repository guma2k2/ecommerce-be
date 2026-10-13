package com.yas.system.payment.internal.service;

import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.entity.PaymentLedger;
import com.yas.system.payment.internal.enumeration.BalanceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PaymentLedgerService {

    PaymentLedger recordInitialization(Payment payment);

    PaymentLedger recordCapture(Payment payment, String gatewayTxId, String rawPayload);

    PaymentLedger recordRefund(Payment payment, BigDecimal refundAmount, String gatewayRefundId, String reason);

    PaymentLedger recordAdjustment(Payment payment, BigDecimal amount, BalanceType balanceType, String reason);

    List<PaymentLedger> getLedgerByPaymentId(UUID paymentId);

    List<PaymentLedger> getLedgerByOrderId(String orderId);
}
