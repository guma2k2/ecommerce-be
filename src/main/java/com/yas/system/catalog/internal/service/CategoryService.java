package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.CategoryCreateRequest;
import com.yas.system.catalog.internal.dto.request.CategoryUpdateRequest;
import com.yas.system.catalog.internal.dto.response.CategoryResponse;
import com.yas.system.common.response.PageResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryCreateRequest categoryRequest);
    CategoryResponse updateCategory(CategoryUpdateRequest categoryRequest, Integer categoryId);
    CategoryResponse getCategoryById(Integer categoryId);
    void deleteCategory(Integer categoryId);
    PageResponse<CategoryResponse> getCategoryPage(Integer pageNumber, Integer pageSize);
    List<CategoryResponse> getAllParentCategories();
    CategoryResponse getCategoryByName(String name);
}

