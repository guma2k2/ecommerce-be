package com.yas.system.order.internal.dto.request;

import com.yas.system.payment.internal.enumeration.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
        @Valid
        @NotNull(message = "Shipping address is required")
        ShippingAddressRequest shippingAddress,

        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @Size(max = 500, message = "Note must not exceed 500 characters")
        String note,

        boolean fromCart,

        List<@Valid OrderItemRequest> items
) {
        public CreateOrderRequest {
                if (items == null) {
                        items = List.of();
                }
        }
}
