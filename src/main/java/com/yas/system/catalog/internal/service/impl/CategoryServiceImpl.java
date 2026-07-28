package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.CategoryCreateRequest;
import com.yas.system.catalog.internal.dto.request.CategoryUpdateRequest;
import com.yas.system.catalog.internal.dto.response.CategoryResponse;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.repository.CategoryRepository;
import com.yas.system.catalog.internal.service.CategoryService;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.common.exception.ResourceNotFoundException;
import com.yas.system.common.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.yas.system.common.util.StringUtils.isBlank;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void createCategory(CategoryCreateRequest categoryRequest) {
        validateCreateCategoryRequest(categoryRequest);

        Category parent = resolveParent(categoryRequest.parentId());
        categoryRepository.save(createCategory(categoryRequest, parent));
    }

    @Override
    @Transactional
    public void updateCategory(CategoryUpdateRequest categoryRequest, Integer categoryId) {
        validateUpdateCategoryRequest(categoryRequest, categoryId);

        Category category = findCategoryById(categoryId);
        Category parent = resolveParent(categoryRequest.parentId());
        validateParent(categoryId, parent);

        updateCategory(categoryRequest, category, parent);
        categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer categoryId) {
        if (Objects.isNull(categoryId)) {
            throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
        }
        Category category = categoryRepository.findByIdCustom(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));

        return CategoryResponse.from(category);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> getCategoryPage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<CategoryResponse> content = categoryPage.getContent().stream()
                .map(CategoryResponse::from)
                .toList();

        return new PageResponse<>(
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalPages(),
                categoryPage.getTotalElements(),
                content
        );
    }

    @Override
    @Transactional
    public void deleteCategory(Integer categoryId) {
        if (Objects.isNull(categoryId)) {
            throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
        }
        Category category = findCategoryById(categoryId);

        categoryRepository.delete(category);
    }

    private void validateCreateCategoryRequest(CategoryCreateRequest categoryRequest) {
        if (Objects.isNull(categoryRequest) || isBlank(categoryRequest.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
        }
        if (categoryRepository.checkExited(categoryRequest.name(), null).isPresent()) {
            throw new InvalidDataException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    private void validateUpdateCategoryRequest(CategoryUpdateRequest categoryRequest, Integer categoryId) {
        if (Objects.isNull(categoryId) || Objects.isNull(categoryRequest) || isBlank(categoryRequest.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
        }
        if (categoryRepository.checkExited(categoryRequest.name(), categoryId).isPresent()) {
            throw new InvalidDataException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    private Category findCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    private Category resolveParent(Integer parentId) {
        if (Objects.isNull(parentId)) {
            return null;
        }
        return findCategoryById(parentId);
    }

    private Category createCategory(CategoryCreateRequest request, Category parent) {
        return Category.builder()
                .name(request.name())
                .parent(parent)
                .build();
    }

    private void updateCategory(CategoryUpdateRequest request, Category category, Category parent) {
        category.setName(request.name());
        category.setParent(parent);
    }

    private void validateParent(Integer categoryId, Category parent) {
        Category currentParent = parent;
        while (Objects.nonNull(currentParent)) {
            if (categoryId.equals(currentParent.getId())) {
                throw new InvalidDataException(ErrorCode.INVALID_CATEGORY);
            }
            currentParent = currentParent.getParent();
        }
    }

}
