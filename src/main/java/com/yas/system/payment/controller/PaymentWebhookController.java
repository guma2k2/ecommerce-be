package com.yas.system.payment.controller;

import com.yas.system.payment.enumeration.PaymentMethod;
import com.yas.system.payment.service.PaymentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments/webhook")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentWebhookController {

    PaymentService paymentService;

    @PostMapping("/{provider}")
    public ResponseEntity<Void> handleGatewayWebhook(
            @PathVariable String provider,
            @RequestBody String payload,
            @RequestHeader Map<String, String> headers
    ) {
        PaymentMethod method = PaymentMethod.from(provider);
        log.info("Received {} webhook request (payload size: {} bytes)", method, payload != null ? payload.length() : 0);
        paymentService.handleWebhookEvent(method, headers, payload);
        return ResponseEntity.ok().build();
    }
}
