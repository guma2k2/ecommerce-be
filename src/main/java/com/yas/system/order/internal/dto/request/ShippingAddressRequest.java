package com.yas.system.order.internal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingAddressRequest(
        @NotBlank(message = "Receiver name is required")
        @Size(max = 100, message = "Receiver name must not exceed 100 characters")
        String receiverName,

        @NotBlank(message = "Receiver phone is required")
        @Size(max = 20, message = "Receiver phone must not exceed 20 characters")
        String receiverPhone,

        @NotBlank(message = "Shipping address is required")
        @Size(max = 255, message = "Shipping address must not exceed 255 characters")
        String shippingAddress,

        @Size(max = 100, message = "City must not exceed 100 characters")
        String city,

        @Size(max = 100, message = "District must not exceed 100 characters")
        String district,

        @Size(max = 20, message = "Postal code must not exceed 20 characters")
        String postalCode
) {
}
