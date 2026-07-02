package com.yas.system.catalog.internal.dto.request;

public record CategoryRequest(
        String name,
        Long parentId
) {
}
