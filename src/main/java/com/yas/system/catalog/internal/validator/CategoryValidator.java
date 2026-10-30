package com.yas.system.catalog.internal.validator;

import com.yas.system.catalog.internal.dto.request.CategoryCreateRequest;
import com.yas.system.catalog.internal.dto.request.CategoryUpdateRequest;
import com.yas.system.catalog.internal.entity.Category;
import com.yas.system.catalog.internal.repository.CategoryRepository;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.yas.system.common.util.StringUtils.isBlank;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryValidator {

    CategoryRepository categoryRepository;

    public void validateCreate(CategoryCreateRequest request) {
        if (Objects.isNull(request) || isBlank(request.name())) {
            throw new ApplicationException(ErrorCode.INVALID_CATEGORY);
        }
        if (categoryRepository.checkExited(request.name(), null).isPresent()) {
            throw new ApplicationException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    public void validateUpdate(CategoryUpdateRequest request, Integer categoryId) {
        validateId(categoryId);
        if (Objects.isNull(request) || isBlank(request.name())) {
            throw new ApplicationException(ErrorCode.INVALID_CATEGORY);
        }
        if (categoryRepository.checkExited(request.name(), categoryId).isPresent()) {
            throw new ApplicationException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
    }

    public void validateId(Integer categoryId) {
        if (Objects.isNull(categoryId)) {
            throw new ApplicationException(ErrorCode.INVALID_CATEGORY);
        }
    }

    public void validateParentHierarchy(Integer categoryId, Category parent) {
        Category currentParent = parent;
        while (Objects.nonNull(currentParent)) {
            if (categoryId.equals(currentParent.getId())) {
                throw new ApplicationException(ErrorCode.INVALID_CATEGORY);
            }
            currentParent = currentParent.getParent();
        }
    }
}

