package com.yas.system.catalog.internal.dto.request;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Map;

public record ProductVariantUpdateRequest(
        Long id,
        String title,
        Map<Long, VariantOptionValueRequest> options, // <ProductOptionId, VariantOptionValue>
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
                .id(id)
                .title(title)
                .sku(sku)
                .price(price)
                .quantity(quantity)
                .product(product)
                .build();
    }

    public void applyTo(ProductVariant variant) {
        variant.setTitle(title);
        variant.setSku(sku);
        variant.setPrice(price);
        variant.setQuantity(quantity);
    }
}
