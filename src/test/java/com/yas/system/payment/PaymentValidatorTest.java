package com.yas.system.payment;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.entity.Payment;
import com.yas.system.payment.internal.enumeration.PaymentStatus;
import com.yas.system.payment.internal.gateway.dto.RefundResult;
import com.yas.system.payment.internal.validator.PaymentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentValidatorTest {

    private PaymentValidator paymentValidator;

    @BeforeEach
    void setUp() {
        paymentValidator = new PaymentValidator();
    }

    @Test
    @DisplayName("validateCustomer throws UNAUTHENTICATED when customer or id is null")
    void validateCustomer_unauthenticated() {
        assertThatThrownBy(() -> paymentValidator.validateCustomer(null))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHENTICATED);

        AuthUser userNoId = new AuthUser(null, "test@yas.com", "USER", "pw", List.of());
        assertThatThrownBy(() -> paymentValidator.validateCustomer(userNoId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHENTICATED);
    }

    @Test
    @DisplayName("validateRefund throws PAYMENT_ALREADY_REFUNDED when status is REFUNDED")
    void validateRefund_alreadyRefunded() {
        Payment payment = Payment.builder()
                .status(PaymentStatus.REFUNDED)
                .amount(BigDecimal.valueOf(100))
                .refundedAmount(BigDecimal.valueOf(100))
                .build();

        UUID paymentId = UUID.randomUUID();
        assertThatThrownBy(() -> paymentValidator.validateRefund(payment, BigDecimal.valueOf(10), paymentId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_ALREADY_REFUNDED);
    }

    @Test
    @DisplayName("validateRefund throws PAYMENT_GATEWAY_ERROR when status is PENDING")
    void validateRefund_invalidStatus() {
        Payment payment = Payment.builder()
                .status(PaymentStatus.PENDING)
                .amount(BigDecimal.valueOf(100))
                .refundedAmount(BigDecimal.ZERO)
                .build();

        UUID paymentId = UUID.randomUUID();
        assertThatThrownBy(() -> paymentValidator.validateRefund(payment, BigDecimal.valueOf(10), paymentId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    @Test
    @DisplayName("validateRefund throws REFUND_AMOUNT_EXCEEDS_PAYMENT when refund amount > remaining")
    void validateRefund_exceedsPayment() {
        Payment payment = Payment.builder()
                .status(PaymentStatus.SUCCEEDED)
                .amount(BigDecimal.valueOf(100))
                .refundedAmount(BigDecimal.valueOf(80))
                .build();

        UUID paymentId = UUID.randomUUID();
        assertThatThrownBy(() -> paymentValidator.validateRefund(payment, BigDecimal.valueOf(30), paymentId))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFUND_AMOUNT_EXCEEDS_PAYMENT);
    }

    @Test
    @DisplayName("validateRefund succeeds when amount <= remaining on SUCCEEDED payment")
    void validateRefund_success() {
        Payment payment = Payment.builder()
                .status(PaymentStatus.SUCCEEDED)
                .amount(BigDecimal.valueOf(100))
                .refundedAmount(BigDecimal.valueOf(20))
                .build();

        UUID paymentId = UUID.randomUUID();
        assertThatCode(() -> paymentValidator.validateRefund(payment, BigDecimal.valueOf(50), paymentId))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateRefundResult throws PAYMENT_GATEWAY_ERROR when unsuccessful")
    void validateRefundResult_failure() {
        RefundResult failureResult = new RefundResult(false, null, null, "Insufficient funds in Stripe balance");

        assertThatThrownBy(() -> paymentValidator.validateRefundResult(failureResult))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAYMENT_GATEWAY_ERROR);
    }

    @Test
    @DisplayName("validateRefundResult succeeds when successful")
    void validateRefundResult_success() {
        RefundResult successResult = new RefundResult(true, "re_12345", BigDecimal.valueOf(50), null);

        assertThatCode(() -> paymentValidator.validateRefundResult(successResult))
                .doesNotThrowAnyException();
    }
}
