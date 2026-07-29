package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.variant.ProductVariant;
import com.yas.system.catalog.internal.entity.variant.VariantOptionValue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public record ProductVariantResponse(
        Long id,
        String title,
        List<VariantOptionValueResponse> options,
        String sku,
        BigDecimal price,
        int quantity
) {
    public static ProductVariantResponse from(ProductVariant variant, List<VariantOptionValue> optionValues) {
        List<VariantOptionValueResponse> optionsList = Objects.isNull(optionValues) ? List.of() : optionValues.stream()
                .filter(optionValue -> Objects.nonNull(optionValue.getProductVariant())
                        && Objects.equals(optionValue.getProductVariant().getId(), variant.getId()))
                .filter(optionValue -> Objects.nonNull(optionValue.getProductOption()))
                .map(optionValue -> new VariantOptionValueResponse(
                        optionValue.getProductOption().getId(),
                        optionValue.getValue(),
                        optionValue.getPosition()
                ))
                .toList();

        return new ProductVariantResponse(
                variant.getId(),
                variant.getTitle(),
                optionsList,
                variant.getSku(),
                variant.getPrice(),
                Objects.nonNull(variant.getQuantity()) ? variant.getQuantity() : 0
        );
    }
}
