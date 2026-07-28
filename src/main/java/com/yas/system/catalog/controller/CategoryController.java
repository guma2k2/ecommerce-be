package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.CategoryCreateRequest;
import com.yas.system.catalog.internal.dto.request.CategoryUpdateRequest;
import com.yas.system.catalog.internal.dto.response.CategoryResponse;
import com.yas.system.catalog.internal.service.CategoryService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
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
    public ApiResponse<Void> createCategory(@RequestBody @Valid CategoryCreateRequest request) {
        categoryService.createCategory(request);
        return ApiResponse.successWithNoContent();
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<Void> updateCategory(
            @RequestBody @Valid CategoryUpdateRequest request,
            @PathVariable Integer categoryId
    ) {
        categoryService.updateCategory(request, categoryId);
        return ApiResponse.successWithNoContent();
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryResponse> getCategory(@PathVariable Integer categoryId) {
        return ApiResponse.success(categoryService.getCategoryById(categoryId));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<CategoryResponse>> getCategoryPage(
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(categoryService.getCategoryPage(pageNumber, pageSize));
    }

    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteById(@PathVariable Integer categoryId) {
        categoryService.deleteCategory(categoryId);
        return ApiResponse.successWithNoContent();
    }
}

