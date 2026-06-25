package com.yas.system.catalog.internal.dto.request;

public record ReviewCreateRequest(
        Long productVariantId,
        String content,
        int ratingStar
) {
}
