package com.yas.system.payment;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.payment.controller.PaymentWebhookController;
import com.yas.system.payment.internal.enumeration.PaymentMethod;
import com.yas.system.payment.internal.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookControllerTest {

    @Mock
    PaymentService paymentService;

    @InjectMocks
    PaymentWebhookController controller;

    @Test
    @DisplayName("handleGatewayWebhook should parse lowercase stripe and delegate to PaymentService")
    void handleGatewayWebhook_lowercaseStripe_success() {
        Map<String, String> headers = Map.of("Stripe-Signature", "sig_123");
        String payload = "{\"id\":\"evt_1\"}";

        ResponseEntity<Void> response = controller.handleGatewayWebhook("stripe", payload, headers);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(paymentService).handleWebhookEvent(PaymentMethod.STRIPE, headers, payload);
    }

    @Test
    @DisplayName("handleGatewayWebhook should throw ApplicationException for unknown provider")
    void handleGatewayWebhook_unknownProvider_throwsException() {
        Map<String, String> headers = Map.of();
        String payload = "{}";

        assertThatThrownBy(() -> controller.handleGatewayWebhook("unknown", payload, headers))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> {
                    ApplicationException appEx = (ApplicationException) ex;
                    assertThat(appEx.getErrorCode()).isEqualTo(ErrorCode.PAYMENT_METHOD_NOT_SUPPORTED);
                });
    }
}
