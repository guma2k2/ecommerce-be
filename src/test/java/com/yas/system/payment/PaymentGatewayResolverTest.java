package com.yas.system.payment;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.payment.enumeration.PaymentMethod;
import com.yas.system.payment.internal.gateway.PaymentGateway;
import com.yas.system.payment.internal.gateway.PaymentGatewayResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayResolverTest {

    @Mock
    PaymentGateway stripeGateway;

    PaymentGatewayResolver resolver;

    @BeforeEach
    void setUp() {
        when(stripeGateway.getPaymentMethod()).thenReturn(PaymentMethod.STRIPE);
        resolver = new PaymentGatewayResolver(List.of(stripeGateway));
    }

    @Test
    @DisplayName("resolve should return corresponding gateway when method is supported")
    void resolve_success() {
        PaymentGateway result = resolver.resolve(PaymentMethod.STRIPE);
        assertThat(result).isSameAs(stripeGateway);
    }

    @Test
    @DisplayName("resolve should throw ApplicationException when method is null")
    void resolve_nullMethod_throwsException() {
        assertThatThrownBy(() -> resolver.resolve(null))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
                });
    }

    @Test
    @DisplayName("resolve should throw ApplicationException when method is unsupported")
    void resolve_unsupportedMethod_throwsException() {
        assertThatThrownBy(() -> resolver.resolve(PaymentMethod.COD))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
                });
    }
}
