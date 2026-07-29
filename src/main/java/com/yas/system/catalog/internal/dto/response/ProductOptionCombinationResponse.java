package com.yas.system.catalog.internal.dto.response;

import java.util.List;

public record ProductOptionCombinationResponse(
        Long id, // product option id
        int position, // product option combination position
        List<String> values // list variant option value
) {
}
