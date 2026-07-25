package com.yas.system.catalog.internal.dto.request;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductVariant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductVariantRequest(
        Long id,
        String title,
        String option1,
        String option2,
        String option3,
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
