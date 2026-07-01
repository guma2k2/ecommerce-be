package com.yas.system.catalog.internal.dto.request;

import com.yas.system.catalog.internal.entity.Product;
import com.yas.system.catalog.internal.entity.ProductOption;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Arrays;

public record ProductOptionRequest (
        Long id,
        @NotBlank
        String name,
        @NotEmpty
        String[] values,
        @Min(1)
        @Max(3)
        int position
) {
    public ProductOption toEntity(Product product) {
        ProductOption option = ProductOption.builder()
                .name(name)
                .values(Arrays.asList(values))
                .position(position)
                .product(product)
                .build();
        option.setId(id);
        return option;
    }

    public void applyTo(ProductOption option) {
        option.setName(name);
        option.setValues(Arrays.asList(values));
        option.setPosition(position);
    }
}
