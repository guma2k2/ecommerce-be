package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductAttributeRequest;
import com.yas.system.catalog.internal.dto.response.ProductAttributeResponse;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.helper.ProductAttributeHelper;
import com.yas.system.catalog.internal.repository.ProductAttributeRepository;
import com.yas.system.catalog.internal.service.ProductAttributeService;
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
public class ProductAttributeServiceImpl implements ProductAttributeService {

    ProductAttributeRepository productAttributeRepository;
    ProductAttributeHelper productAttributeHelper;

    @Override
    @Transactional
    public void createProductAttribute(ProductAttributeRequest request) {
        validateCreateProductAttributeRequest(request);
        productAttributeRepository.save(productAttributeHelper.createProductAttribute(request));
    }

    @Override
    @Transactional
    public void updateProductAttribute(ProductAttributeRequest request, Long productAttributeId) {
        validateUpdateProductAttributeRequest(request, productAttributeId);

        ProductAttribute productAttribute = findProductAttributeById(productAttributeId);
        productAttributeHelper.updateProductAttribute(request, productAttribute);
        productAttributeRepository.save(productAttribute);
    }

    @Override
    @Transactional
    public void deleteProductAttributeById(Long productAttributeId) {
        if (Objects.isNull(productAttributeId)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_ATTRIBUTE);
        }
        ProductAttribute productAttribute = findProductAttributeById(productAttributeId);

        productAttributeRepository.delete(productAttribute);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAttributeResponse getById(Long productAttributeId) {
        if (Objects.isNull(productAttributeId)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_ATTRIBUTE);
        }
        return ProductAttributeResponse.from(findProductAttributeById(productAttributeId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductAttributeResponse> getProductAttributePage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductAttribute> productAttributePage = productAttributeRepository.findAll(pageable);

        List<ProductAttributeResponse> content = productAttributePage.getContent().stream()
                .map(ProductAttributeResponse::from)
                .toList();

        return new PageResponse<>(
                productAttributePage.getNumber(),
                productAttributePage.getSize(),
                productAttributePage.getTotalPages(),
                productAttributePage.getTotalElements(),
                content
        );
    }


    private void validateCreateProductAttributeRequest(ProductAttributeRequest request) {
        if (Objects.isNull(request) || isBlank(request.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_ATTRIBUTE);
        }
        if (productAttributeRepository.checkExited(request.name(), null).isPresent()) {
            throw new InvalidDataException(ErrorCode.PRODUCT_ATTRIBUTE_ALREADY_EXISTS);
        }
    }

    private void validateUpdateProductAttributeRequest(ProductAttributeRequest request, Long productAttributeId) {
        if (Objects.isNull(productAttributeId) || Objects.isNull(request) || isBlank(request.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_ATTRIBUTE);
        }
        if (productAttributeRepository.checkExited(request.name(), productAttributeId).isPresent()) {
            throw new InvalidDataException(ErrorCode.PRODUCT_ATTRIBUTE_ALREADY_EXISTS);
        }
    }

    private ProductAttribute findProductAttributeById(Long productAttributeId) {
        return productAttributeRepository.findById(productAttributeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_ATTRIBUTE_NOT_FOUND));
    }


}
