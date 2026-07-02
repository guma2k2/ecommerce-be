package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.CategoryRequest;
import com.yas.system.catalog.internal.dto.response.CategoryResponse;

public interface CategoryService {
    void createCategory(CategoryRequest categoryRequest);
    void updateCategory(CategoryRequest categoryRequest, Long categoryId);
    CategoryResponse getCategoryById(Long categoryId);
    void deleteCategory(Long categoryId);
}
