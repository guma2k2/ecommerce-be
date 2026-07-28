package com.yas.system.catalog.internal.dto.request;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductOption;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProductOptionCreateRequest(
        @NotBlank
        String name,
        @NotEmpty
        List<String> values,
        @Min(1)
        @Max(3)
        int position
) {
    public ProductOption toEntity(Product product) {
        return ProductOption.builder()
                .name(name)
                .values(values)
                .position(position)
                .product(product)
                .build();
    }
}
