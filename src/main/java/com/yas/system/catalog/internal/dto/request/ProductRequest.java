package com.yas.system.catalog.internal.dto.request;

import com.yas.system.catalog.internal.entity.Product;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ProductRequest(
        @NotBlank
        String name,
        String description,
        @NotBlank
        String slug,
        @Valid
        @NotEmpty
        ProductVariantRequest[] variants,
        @Valid
        @NotEmpty
        ProductOptionRequest[] options
) {
    public Product toEntity() {
        return Product.builder()
                .name(name)
                .description(description)
                .slug(slug)
                .build();
    }

    public void applyTo(Product product) {
        product.setName(name);
        product.setDescription(description);
        product.setSlug(slug);
    }
}
