package com.yas.system.payment.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.security.annotation.ActiveUser;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.payment.internal.dto.request.CreateCheckoutSessionRequest;
import com.yas.system.payment.internal.dto.response.CheckoutSessionResponse;
import com.yas.system.payment.internal.dto.response.PaymentResponse;
import com.yas.system.payment.internal.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentService paymentService;

    @PostMapping("/checkout-session")
    public ApiResponse<CheckoutSessionResponse> createCheckoutSession(
            @ActiveUser AuthUser customer,
            @Valid @RequestBody CreateCheckoutSessionRequest request
    ) {
        return ApiResponse.success(paymentService.createCheckoutSession(customer, request));
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> getPayment(
            @ActiveUser AuthUser customer,
            @PathVariable Long paymentId
    ) {
        return ApiResponse.success(paymentService.getPaymentById(customer, paymentId));
    }

    @GetMapping
    public ApiResponse<List<PaymentResponse>> getCustomerPayments(@ActiveUser AuthUser customer) {
        return ApiResponse.success(paymentService.getCustomerPayments(customer));
    }
}
