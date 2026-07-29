package com.yas.system.catalog.internal.dto.request;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantCreateRequest(
        String title,
        Map<Long, VariantOptionValueRequest> options, // <ProductOptionId, VariantOptionValueRequest>
        @NotBlank
        String sku,
        @NotNull
        @PositiveOrZero
        BigDecimal price,
        @PositiveOrZero
        int quantity
) {
    public ProductVariant toEntity(Product product) {
        return ProductVariant.builder()
                .title(title)
                .sku(sku)
                .price(price)
                .quantity(quantity)
                .product(product)
                .build();
    }
}
