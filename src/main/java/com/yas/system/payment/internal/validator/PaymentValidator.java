package com.yas.system.payment.internal.validator;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.gateway.dto.RefundResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentValidator {

    public void validateCustomer(AuthUser customer) {
        if (customer == null || customer.id() == null) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
        }
    }

    public void validateRefund(Payment payment, BigDecimal refundAmount, UUID paymentId) {
        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new ApplicationException(ErrorCode.PAYMENT_ALREADY_REFUNDED, paymentId);
        }

        if (payment.getStatus() != PaymentStatus.SUCCEEDED && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new ApplicationException(ErrorCode.PAYMENT_GATEWAY_ERROR, "Cannot refund payment in status: " + payment.getStatus());
        }

        BigDecimal newTotalRefunded = payment.getRefundedAmount().add(refundAmount);
        if (newTotalRefunded.compareTo(payment.getAmount()) > 0) {
            throw new ApplicationException(
                    ErrorCode.REFUND_AMOUNT_EXCEEDS_PAYMENT,
                    refundAmount,
                    payment.getAmount().subtract(payment.getRefundedAmount())
            );
        }
    }

    public void validateRefundResult(RefundResult refundResult) {
        if (!refundResult.successful()) {
            throw new ApplicationException(ErrorCode.PAYMENT_GATEWAY_ERROR, refundResult.failureReason());
        }
    }
}

