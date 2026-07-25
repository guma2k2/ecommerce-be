package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.CategoryRequest;
import com.yas.system.catalog.internal.dto.response.CategoryResponse;
import com.yas.system.common.response.PageResponse;

public interface CategoryService {
    void createCategory(CategoryRequest categoryRequest);
    void updateCategory(CategoryRequest categoryRequest, Integer categoryId);
    CategoryResponse getCategoryById(Integer categoryId);
    void deleteCategory(Integer categoryId);
    PageResponse<CategoryResponse> getCategoryPage(Integer pageNumber, Integer pageSize);
}

