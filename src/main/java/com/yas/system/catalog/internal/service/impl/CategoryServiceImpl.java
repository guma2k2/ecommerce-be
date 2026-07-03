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

import java.util.List;
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
    @Transactional
    public void updateCategory(CategoryRequest categoryRequest, Long categoryId) {
        validateUpdateCategoryRequest(categoryRequest, categoryId);

        Category category = findCategoryById(categoryId);
        Category parent = resolveParent(categoryRequest.parentId());
        validateParent(categoryId, parent);

        updateCategory(categoryRequest, category, parent);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long categoryId) {
        if (Objects.isNull(categoryId)) {
            throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
        }
        Category category = categoryRepository.findByIdCustom(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        return toCategoryResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (Objects.isNull(categoryId)) {
            throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
        }
        Category category = findCategoryById(categoryId);

        categoryRepository.delete(category);
    }

    private void validateCreateCategoryRequest(CategoryRequest categoryRequest) {
        if (Objects.isNull(categoryRequest) || isBlank(categoryRequest.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
        }
        if (categoryRepository.checkExited(categoryRequest.name(), null).isPresent()) {
            throw new InvalidDataException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    private void validateUpdateCategoryRequest(CategoryRequest categoryRequest, Long categoryId) {
        if (Objects.isNull(categoryId) || Objects.isNull(categoryRequest) || isBlank(categoryRequest.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
        }
        if (categoryRepository.checkExited(categoryRequest.name(), categoryId).isPresent()) {
            throw new InvalidDataException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Category resolveParent(Long parentId) {
        if (Objects.isNull(parentId)) {
            return null;
        }
        return findCategoryById(parentId);
    }

    private Category createCategory(CategoryRequest request, Category parent) {
        return Category.builder()
                .name(request.name())
                .parent(parent)
                .build();
    }

    private void updateCategory(CategoryRequest request, Category category, Category parent) {
        category.setName(request.name());
        category.setParent(parent);
    }

    private void validateParent(Long categoryId, Category parent) {
        Category currentParent = parent;
        while (Objects.nonNull(currentParent)) {
            if (categoryId.equals(currentParent.getId())) {
                throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
            }
            currentParent = currentParent.getParent();
        }
    }

    private CategoryResponse toCategoryResponse(Category category) {
        List<CategoryResponse> children = Objects.isNull(category.getChildren())
                ? List.of()
                : category.getChildren().stream()
                .map(this::toCategoryResponse)
                .toList();

        return new CategoryResponse(
                category.getId(),
                category.getName(),
                children
        );
    }

    private boolean isBlank(String value) {
        return Objects.isNull(value) || value.isBlank();
    }
}
