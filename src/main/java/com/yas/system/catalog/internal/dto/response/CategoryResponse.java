package com.yas.system.catalog.internal.dto.response;

import java.util.List;

public record CategoryResponse (
        Long id,
        String name,
        List<CategoryResponse> children
) {
}
