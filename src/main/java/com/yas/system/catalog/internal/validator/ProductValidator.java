package com.yas.system.catalog.internal.validator;

import com.yas.system.catalog.internal.dto.request.ProductCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductUpdateRequest;
import com.yas.system.catalog.internal.repository.ProductRepository;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductValidator {

    ProductRepository productRepository;

    public void validateCreate(ProductCreateRequest request) {
        log.debug("Validating uniqueness of name: '{}' and slug: '{}' for product creation", request.name(), request.slug());
        if (productRepository.existsByName(request.name())) {
            throw new ApplicationException(ErrorCode.PRODUCT_NAME_ALREADY_EXISTS);
        }
        if (productRepository.existsBySlug(request.slug())) {
            throw new ApplicationException(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
        }
    }

    public void validateUpdate(ProductUpdateRequest request, Long productId) {
        validateId(productId);
        log.debug("Validating uniqueness of name: '{}' and slug: '{}' for product update ID: {}", request.name(), request.slug(), productId);
        if (productRepository.existsByNameAndIdNot(request.name(), productId)) {
            throw new ApplicationException(ErrorCode.PRODUCT_NAME_ALREADY_EXISTS);
        }
        if (productRepository.existsBySlugAndIdNot(request.slug(), productId)) {
            throw new ApplicationException(ErrorCode.PRODUCT_SLUG_ALREADY_EXISTS);
        }
    }

    public void validateId(Long productId) {
        if (Objects.isNull(productId)) {
            throw new ApplicationException(ErrorCode.INVALID_PRODUCT);
        }
    }
}

