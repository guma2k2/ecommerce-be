package com.yas.system.order.internal.dto.response;

public record ShippingAddressResponse(
        String receiverName,
        String receiverPhone,
        String shippingAddress,
        String city,
        String district,
        String postalCode
) {
}
