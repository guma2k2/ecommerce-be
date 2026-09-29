package com.yas.system.cart.internal.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartId,
        ProductVariantResponse variant,
        int quantity,
        BigDecimal subtotal
) {
}
