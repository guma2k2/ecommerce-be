package com.yas.system.catalog.internal.dto.response;

import java.util.List;

public record ProductOptionCombinationResponse(
        Long id, // product option id
        String name, // product option name
        int position, // product option combination position
        List<ProductOptionValueResponse> values
) {
}
