package com.yas.system.payment;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentMethodTest {

    @ParameterizedTest
    @ValueSource(strings = {"stripe", "STRIPE", "Stripe", " stripe "})
    @DisplayName("from should parse case-insensitively for valid payment methods")
    void from_caseInsensitive_success(String input) {
        assertThat(PaymentMethod.from(input)).isEqualTo(PaymentMethod.STRIPE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"momo", "paypal", "invalid", "", "   "})
    @DisplayName("from should throw ApplicationException for unsupported or blank methods")
    void from_unsupportedOrBlank_throwsException(String input) {
        assertThatThrownBy(() -> PaymentMethod.from(input))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
                });
    }

    @Test
    @DisplayName("from should throw ApplicationException when input is null")
    void from_null_throwsException() {
        assertThatThrownBy(() -> PaymentMethod.from(null))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
                });
    }
}
