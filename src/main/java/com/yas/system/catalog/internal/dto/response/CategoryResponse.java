package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.Category;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public record CategoryResponse (
        Long id,
        String name,
        List<CategoryResponse> children,
        String createdAt,
        String updatedAt
) {
    public static CategoryResponse from(Category category) {
        List<CategoryResponse> children = Objects.isNull(category.getChildren())
                ? List.of()
                : category.getChildren().stream()
                .map(CategoryResponse::from)
                .toList();

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                children,
                category.getCreatedAt() != null ? category.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                category.getUpdatedAt() != null ? category.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        );
    }
}
