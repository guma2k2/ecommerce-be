package com.yas.system.catalog.internal.dto.request;

public record CategoryCreateRequest(
        String name,
        Integer parentId
) {
}
