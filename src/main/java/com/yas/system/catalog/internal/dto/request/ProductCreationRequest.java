package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record ProductCreationRequest(
    @NotBlank
    String name,

    String description,

    int brandId,

    int categoryId,

    Map<Integer, String> attributes,
    List<ProductVariantCreateRequest> productVariants,

    List<ProductImageCreateRequest> productImages

) {
}
