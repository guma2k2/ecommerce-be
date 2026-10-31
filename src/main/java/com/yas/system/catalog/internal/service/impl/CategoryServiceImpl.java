package com.yas.system.catalog.service.impl;

import com.yas.system.catalog.internal.dto.request.CategoryCreateRequest;
import com.yas.system.catalog.internal.dto.request.CategoryUpdateRequest;
import com.yas.system.catalog.internal.dto.response.CategoryResponse;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.repository.CategoryRepository;
import com.yas.system.catalog.service.CategoryService;
import com.yas.system.catalog.internal.validator.CategoryValidator;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static com.yas.system.common.util.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryServiceImpl implements CategoryService {

    CategoryRepository categoryRepository;
    CategoryValidator categoryValidator;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest categoryRequest) {
        categoryValidator.validateCreate(categoryRequest);

        Category parent = resolveParent(categoryRequest.parentId());
        Category savedCategory = categoryRepository.save(createCategory(categoryRequest, parent));
        return CategoryResponse.from(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(CategoryUpdateRequest categoryRequest, Integer categoryId) {
        categoryValidator.validateUpdate(categoryRequest, categoryId);

        Category category = findCategoryById(categoryId);
        Category parent = resolveParent(categoryRequest.parentId());
        categoryValidator.validateParentHierarchy(categoryId, parent);

        updateCategory(categoryRequest, category, parent);
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponse.from(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Integer categoryId) {
        categoryValidator.validateId(categoryId);
        Category category = categoryRepository.findByIdCustom(categoryId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));

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
        categoryValidator.validateId(categoryId);
        Category category = findCategoryById(categoryId);

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllParentCategories() {
        return categoryRepository.findAllCategoryParents().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryByName(String name) {
        if (isBlank(name)) {
            throw new ApplicationException(ErrorCode.INVALID_CATEGORY);
        }
        Category category = categoryRepository.findByNameIgnoreCaseCustom(name.trim())
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));

        return CategoryResponse.from(category);
    }

    private Category findCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.CATEGORY_NOT_FOUND));
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
}
