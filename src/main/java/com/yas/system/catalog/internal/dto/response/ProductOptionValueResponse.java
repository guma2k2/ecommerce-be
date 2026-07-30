package com.yas.system.catalog.internal.dto.response;

public record ProductOptionValueResponse(
        Long id, // ProductOptionValue id
        String value,
        int position
) {
}
