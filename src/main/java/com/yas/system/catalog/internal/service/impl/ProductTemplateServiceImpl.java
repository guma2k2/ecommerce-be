package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductTemplateCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductTemplateUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductTemplateResponse;
import com.yas.system.catalog.internal.entity.attribute.ProductAttribute;
import com.yas.system.catalog.internal.entity.attribute.ProductAttributeTemplate;
import com.yas.system.catalog.internal.entity.attribute.ProductTemplate;
import com.yas.system.catalog.internal.helper.ProductTemplateHelper;
import com.yas.system.catalog.internal.repository.ProductAttributeRepository;
import com.yas.system.catalog.internal.repository.ProductAttributeTemplateRepository;
import com.yas.system.catalog.internal.repository.ProductTemplateRepository;
import com.yas.system.catalog.internal.service.ProductTemplateService;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTemplateServiceImpl implements ProductTemplateService {

    ProductTemplateRepository productTemplateRepository;
    ProductAttributeRepository productAttributeRepository;
    ProductAttributeTemplateRepository productAttributeTemplateRepository;
    ProductTemplateHelper productTemplateHelper;

    @Override
    @Transactional
    public void createProductTemplate(ProductTemplateCreateRequest request) {
        validateCreateProductTemplateRequest(request);

        Map<Long, ProductAttribute> productAttributeById = findProductAttributes(request.attributeIds());
        ProductTemplate productTemplate = productTemplateRepository.save(productTemplateHelper.createProductTemplate(request));

        saveProductAttributeTemplates(productTemplate, request.attributeIds(), productAttributeById);
    }

    @Override
    @Transactional
    public void updateProductTemplate(ProductTemplateUpdateRequest request, Integer productTemplateId) {
        validateUpdateProductTemplateRequest(request, productTemplateId);

        Map<Long, ProductAttribute> productAttributeById = findProductAttributes(request.attributeIds());
        ProductTemplate productTemplate = findProductTemplateById(productTemplateId);
        productTemplateHelper.updateProductTemplate(request, productTemplate);
        productTemplateRepository.save(productTemplate);

        productAttributeTemplateRepository.deleteByProductTemplateId(productTemplateId);
        saveProductAttributeTemplates(productTemplate, request.attributeIds(), productAttributeById);
    }

    @Override
    @Transactional
    public void deleteProductTemplateById(Integer productTemplateId) {
        if (Objects.isNull(productTemplateId)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        ProductTemplate productTemplate = findProductTemplateById(productTemplateId);

        productAttributeTemplateRepository.deleteByProductTemplateId(productTemplateId);
        productTemplateRepository.delete(productTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductTemplateResponse getById(Integer productTemplateId) {
        if (Objects.isNull(productTemplateId)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        return ProductTemplateResponse.from(findProductTemplateById(productTemplateId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductTemplateResponse> getProductTemplatePage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductTemplate> productTemplatePage = productTemplateRepository.findAll(pageable);

        List<ProductTemplateResponse> content = productTemplatePage.getContent().stream()
                .map(ProductTemplateResponse::from)
                .toList();

        return new PageResponse<>(
                productTemplatePage.getNumber(),
                productTemplatePage.getSize(),
                productTemplatePage.getTotalPages(),
                productTemplatePage.getTotalElements(),
                content
        );
    }


    private void validateCreateProductTemplateRequest(ProductTemplateCreateRequest request) {
        if (Objects.isNull(request) || isBlank(request.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        if (productTemplateRepository.checkExited(request.name(), null).isPresent()) {
            throw new InvalidDataException(ErrorCode.PRODUCT_TEMPLATE_ALREADY_EXISTS);
        }
    }

    private void validateUpdateProductTemplateRequest(ProductTemplateUpdateRequest request, Integer productTemplateId) {
        if (Objects.isNull(productTemplateId) || Objects.isNull(request) || isBlank(request.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        if (productTemplateRepository.checkExited(request.name(), productTemplateId).isPresent()) {
            throw new InvalidDataException(ErrorCode.PRODUCT_TEMPLATE_ALREADY_EXISTS);
        }
    }

    private ProductTemplate findProductTemplateById(Integer productTemplateId) {
        return productTemplateRepository.findById(productTemplateId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_TEMPLATE_NOT_FOUND));
    }

    private Map<Long, ProductAttribute> findProductAttributes(List<Long> attributeIds) {
        if (Objects.isNull(attributeIds) || attributeIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> nonNullIds = attributeIds.stream().filter(Objects::nonNull).distinct().toList();
        if (nonNullIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, ProductAttribute> productAttributeById = productAttributeRepository.findAllById(nonNullIds)
                .stream()
                .collect(Collectors.toMap(ProductAttribute::getId, Function.identity()));
        if (productAttributeById.size() != nonNullIds.size()) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_ATTRIBUTE_NOT_FOUND);
        }
        return productAttributeById;
    }

    private void saveProductAttributeTemplates(
            ProductTemplate productTemplate,
            List<Long> attributeIds,
            Map<Long, ProductAttribute> productAttributeById
    ) {
        if (Objects.isNull(attributeIds) || attributeIds.isEmpty()) {
            return;
        }
        List<ProductAttributeTemplate> attributeTemplates = new ArrayList<>();
        for (int i = 0; i < attributeIds.size(); i++) {
            Long attributeId = attributeIds.get(i);
            if (Objects.nonNull(attributeId)) {
                ProductAttribute productAttribute = productAttributeById.get(attributeId);
                if (Objects.nonNull(productAttribute)) {
                    attributeTemplates.add(productTemplateHelper.createProductAttributeTemplate(productTemplate, productAttribute, i));
                }
            }
        }
        if (!attributeTemplates.isEmpty()) {
            productAttributeTemplateRepository.saveAll(attributeTemplates);
        }
    }
}
