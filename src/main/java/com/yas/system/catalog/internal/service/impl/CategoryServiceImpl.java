package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.CategoryRequest;
import com.yas.system.catalog.internal.dto.response.CategoryResponse;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.repository.CategoryRepository;
import com.yas.system.catalog.internal.service.CategoryService;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.common.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void createCategory(CategoryRequest categoryRequest) {
        validateCreateCategoryRequest(categoryRequest);

        Category parent = resolveParent(categoryRequest.parentId());
        categoryRepository.save(createCategory(categoryRequest, parent));
    }

    @Override
    public void updateCategory(CategoryRequest categoryRequest, Long categoryId) {

    }

    @Override
    public CategoryResponse getCategoryById(Long categoryId) {
        return null;
    }

    @Override
    public void deleteCategory(Long categoryId) {

    }

    private void validateCreateCategoryRequest(CategoryRequest categoryRequest) {
        if (Objects.isNull(categoryRequest) || isBlank(categoryRequest.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
        }
        if (categoryRepository.checkExited(categoryRequest.name(), null).isPresent()) {
            throw new InvalidDataException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    private Category resolveParent(Long parentId) {
        if (Objects.isNull(parentId)) {
            return null;
        }
        return categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Category createCategory(CategoryRequest request, Category parent) {
        return Category.builder()
                .name(request.name())
                .parent(parent)
                .build();
    }

    private boolean isBlank(String value) {
        return Objects.isNull(value) || value.isBlank();
    }
}
