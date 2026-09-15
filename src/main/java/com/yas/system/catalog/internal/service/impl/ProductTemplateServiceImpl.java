package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductTemplateCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductTemplateUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductAttributeResponse;
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
    public ProductTemplateResponse createProductTemplate(ProductTemplateCreateRequest request) {
        validateCreateProductTemplateRequest(request);

        Map<Long, ProductAttribute> productAttributeById = findProductAttributes(request.attributeIds());
        ProductTemplate productTemplate = productTemplateRepository.save(productTemplateHelper.createProductTemplate(request));

        saveProductAttributeTemplates(productTemplate, request.attributeIds(), productAttributeById);
        List<ProductAttributeResponse> attributes = buildProductAttributeResponses(request.attributeIds(), productAttributeById);
        return ProductTemplateResponse.from(productTemplate, attributes);
    }

    @Override
    @Transactional
    public ProductTemplateResponse updateProductTemplate(ProductTemplateUpdateRequest request, Integer productTemplateId) {
        validateUpdateProductTemplateRequest(request, productTemplateId);

        Map<Long, ProductAttribute> productAttributeById = findProductAttributes(request.attributeIds());
        ProductTemplate productTemplate = findProductTemplateById(productTemplateId);
        productTemplateHelper.updateProductTemplate(request, productTemplate);
        ProductTemplate savedProductTemplate = productTemplateRepository.save(productTemplate);

        productAttributeTemplateRepository.deleteByProductTemplateId(productTemplateId);
        saveProductAttributeTemplates(savedProductTemplate, request.attributeIds(), productAttributeById);
        List<ProductAttributeResponse> attributes = buildProductAttributeResponses(request.attributeIds(), productAttributeById);
        return ProductTemplateResponse.from(savedProductTemplate, attributes);
    }

    @Override
    @Transactional
    public void deleteProductTemplateById(Integer productTemplateId) {
        if (Objects.isNull(productTemplateId)) {
            throw new ApplicationException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        ProductTemplate productTemplate = findProductTemplateById(productTemplateId);

        productAttributeTemplateRepository.deleteByProductTemplateId(productTemplateId);
        productTemplateRepository.delete(productTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductTemplateResponse getById(Integer productTemplateId) {
        if (Objects.isNull(productTemplateId)) {
            throw new ApplicationException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        ProductTemplate productTemplate = findProductTemplateById(productTemplateId);
        List<ProductAttributeTemplate> attributeTemplates = productAttributeTemplateRepository.findByProductTemplateId(productTemplateId);
        List<ProductAttributeResponse> attributes = attributeTemplates.stream()
                .map(ProductAttributeTemplate::getProductAttribute)
                .filter(Objects::nonNull)
                .map(ProductAttributeResponse::from)
                .toList();
        return ProductTemplateResponse.from(productTemplate, attributes);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductTemplateResponse> getProductTemplatePage(Integer pageNumber, Integer pageSize, Boolean isIncludeAttributes) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductTemplate> productTemplatePage = productTemplateRepository.findAll(pageable);

        if (productTemplatePage.isEmpty()) {
            return new PageResponse<>(
                    productTemplatePage.getNumber(),
                    productTemplatePage.getSize(),
                    productTemplatePage.getTotalPages(),
                    productTemplatePage.getTotalElements(),
                    Collections.emptyList()
            );
        }

        if (Boolean.FALSE.equals(isIncludeAttributes) || Objects.isNull(isIncludeAttributes)) {
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

        List<Integer> templateIds = productTemplatePage.getContent().stream()
                .map(ProductTemplate::getId)
                .toList();

        List<ProductAttributeTemplate> attributeTemplates = productAttributeTemplateRepository.findByProductTemplateIdIn(templateIds);

        Map<Integer, List<ProductAttributeResponse>> attributesByTemplateId = attributeTemplates.stream()
                .filter(pat -> Objects.nonNull(pat.getProductTemplate()) && Objects.nonNull(pat.getProductAttribute()))
                .collect(Collectors.groupingBy(
                        pat -> pat.getProductTemplate().getId(),
                        Collectors.mapping(
                                pat -> ProductAttributeResponse.from(pat.getProductAttribute()),
                                Collectors.toList()
                        )
                ));

        List<ProductTemplateResponse> content = productTemplatePage.getContent().stream()
                .map(pt -> ProductTemplateResponse.from(pt, attributesByTemplateId.getOrDefault(pt.getId(), Collections.emptyList())))
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
            throw new ApplicationException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        if (productTemplateRepository.checkExited(request.name(), null).isPresent()) {
            throw new ApplicationException(ErrorCode.PRODUCT_TEMPLATE_ALREADY_EXISTS);
        }
    }

    private void validateUpdateProductTemplateRequest(ProductTemplateUpdateRequest request, Integer productTemplateId) {
        if (Objects.isNull(productTemplateId) || Objects.isNull(request) || isBlank(request.name())) {
            throw new ApplicationException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        if (productTemplateRepository.checkExited(request.name(), productTemplateId).isPresent()) {
            throw new ApplicationException(ErrorCode.PRODUCT_TEMPLATE_ALREADY_EXISTS);
        }
    }

    private ProductTemplate findProductTemplateById(Integer productTemplateId) {
        return productTemplateRepository.findById(productTemplateId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_TEMPLATE_NOT_FOUND));
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
            throw new ApplicationException(ErrorCode.PRODUCT_ATTRIBUTE_NOT_FOUND);
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

    private List<ProductAttributeResponse> buildProductAttributeResponses(
            List<Long> attributeIds,
            Map<Long, ProductAttribute> productAttributeById
    ) {
        if (Objects.isNull(attributeIds) || attributeIds.isEmpty()) {
            return Collections.emptyList();
        }
        return attributeIds.stream()
                .filter(Objects::nonNull)
                .map(productAttributeById::get)
                .filter(Objects::nonNull)
                .map(ProductAttributeResponse::from)
                .toList();
    }
}
