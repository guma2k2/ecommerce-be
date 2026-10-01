package com.yas.system.cart.api.dto;

public record CartItemPublicDto(
        Long cartId,
        Long productVariantId,
        int quantity
) {
}
