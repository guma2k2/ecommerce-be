package com.yas.system.catalog.internal.dto.request;

import com.tiki.product.dto.enums.ProductImageType;

public record ProductImageCreateRequest(
    String url,
    ProductImageType type,
    int sortOrder,
    boolean status,
    Long productVariantId
) {
}
