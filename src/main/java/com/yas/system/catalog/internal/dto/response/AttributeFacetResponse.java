package com.yas.system.catalog.internal.dto.response;

import java.util.List;

public record AttributeFacetResponse(
        Long id,
        String name,
        List<FacetValueResponse> values
) {
}
