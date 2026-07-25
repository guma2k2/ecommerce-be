package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.ProductVariant;
import com.yas.system.catalog.internal.entity.VariantOptionValue;

import java.math.BigDecimal;
import java.util.List;

public record ProductVariantResponse(
        Long id,
        String option1,
        String option2,
        String option3,
        String sku,
        BigDecimal price,
        int quantity
) {
    public static ProductVariantResponse from(ProductVariant variant, List<VariantOptionValue> optionValues) {
        return new ProductVariantResponse(
                variant.getId(),
                findOptionValue(variant, optionValues, 1),
                findOptionValue(variant, optionValues, 2),
                findOptionValue(variant, optionValues, 3),
                variant.getSku(),
                variant.getPrice(),
                variant.getQuantity()
        );
    }

    private static String findOptionValue(
            ProductVariant variant,
            List<VariantOptionValue> optionValues,
            int optionPosition
    ) {
        return optionValues.stream()
                .filter(optionValue -> optionValue.getProductVariant().equals(variant))
                .filter(optionValue -> optionValue.getProductOption().getPosition() == optionPosition)
                .map(VariantOptionValue::getValue)
                .findFirst()
                .orElse(null);
    }
}
