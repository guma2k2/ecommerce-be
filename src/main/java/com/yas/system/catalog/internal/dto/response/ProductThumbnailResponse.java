package com.yas.system.catalog.internal.dto.response;

public record ProductThumbnailResponse (
        Long id,
        String name,
        String status,
        String createdAt,
        String updatedAt
) {
}
