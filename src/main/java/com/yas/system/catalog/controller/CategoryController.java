package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.CategoryRequest;
import com.yas.system.catalog.internal.dto.response.CategoryResponse;
import com.yas.system.catalog.internal.dto.response.ProductResponse;
import com.yas.system.catalog.internal.service.CategoryService;
import com.yas.system.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/categories")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CategoryController {

    CategoryService categoryService;

    @PostMapping()
    public ApiResponse<Void> createCategory(@RequestBody @Valid CategoryRequest request) {
        categoryService.createCategory(request);
        return ApiResponse.successWithNoContent();
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<Void> updateCategory(
            @RequestBody @Valid CategoryRequest request,
            @PathVariable Long categoryId
    ) {
        categoryService.updateCategory(request, categoryId);
        return ApiResponse.successWithNoContent();
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Long categoryId) {
        return ApiResponse.success(categoryService.getCategoryById(categoryId));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteById(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ApiResponse.successWithNoContent();
    }
}
