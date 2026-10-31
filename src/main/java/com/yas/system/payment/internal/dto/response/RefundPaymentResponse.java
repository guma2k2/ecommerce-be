package com.yas.system.payment.internal.dto.response;

import com.yas.system.payment.enumeration.PaymentStatus;
import com.yas.system.payment.internal.gateway.dto.RefundResult;

import java.math.BigDecimal;
import java.util.UUID;

public record RefundPaymentResponse(
        UUID paymentId,
        String orderId,
        BigDecimal refundedAmount,
        BigDecimal refundableAmount,
        PaymentStatus paymentStatus,
        String gatewayRefundId
) {
    public static RefundPaymentResponse from(UUID paymentId, String orderId, RefundResult result,
                                             BigDecimal newRefundedAmount, BigDecimal refundableAmount,
                                             PaymentStatus newStatus) {
        return new RefundPaymentResponse(
                paymentId,
                orderId,
                newRefundedAmount,
                refundableAmount,
                newStatus,
                result.gatewayRefundId()
        );
    }
}
