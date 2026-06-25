package com.yas.system.catalog.internal.dto.response;

import com.tiki.product.entity.Category;

import java.util.List;

public record CategoryGetByIdResponse (
        Integer id,
        String name,
        List<CategoryResponse> children

){

    public static CategoryGetByIdResponse from(Category category, List<CategoryResponse> children) {
        return new CategoryGetByIdResponse(category.getId(), category.getName(), children);
    }
}
