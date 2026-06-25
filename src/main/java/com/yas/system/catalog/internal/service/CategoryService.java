package com.yas.system.catalog.internal.service;

import com.yas.system.catalog.internal.dto.request.CategoryCreateRequest;
import com.yas.system.catalog.internal.dto.response.AttributeResponse;
import com.yas.system.catalog.internal.dto.response.CategoryGetByIdResponse;
import com.yas.system.catalog.internal.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    void createCategory(CategoryCreateRequest request);
    List<CategoryResponse> findAllCategoryParents();
    CategoryGetByIdResponse getById(Integer id);
    List<AttributeResponse> findAttributesByCategoryId(Integer categoryId);
}
