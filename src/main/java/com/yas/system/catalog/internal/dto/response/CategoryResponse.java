package com.yas.system.catalog.internal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yas.system.catalog.internal.entity.Category;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryResponse (
        Integer id,
        String name,
        List<CategoryResponse> children,
        String createdAt,
        String updatedAt
) {
    public static CategoryResponse from(Category category, boolean includeChildren) {
        if (category == null) {
            return null;
        }

        List<CategoryResponse> children = null;
        if (includeChildren) {
            children = Objects.isNull(category.getChildren())
                    ? List.of()
                    : category.getChildren().stream()
                    .map(CategoryResponse::from)
                    .toList();
        }

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                children,
                category.getCreatedAt() != null ? category.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null,
                category.getUpdatedAt() != null ? category.getUpdatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null
        );
    }

    public static CategoryResponse from(Category category) {
        return from(category, true);
    }

    public static CategoryResponse fromWithoutChildren(Category category) {
        return from(category, false);
    }
}
