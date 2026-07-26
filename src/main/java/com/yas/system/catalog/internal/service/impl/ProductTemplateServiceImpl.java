package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductTemplateRequest;
import com.yas.system.catalog.internal.dto.response.ProductTemplateResponse;
import com.yas.system.catalog.internal.entity.attribute.ProductTemplate;
import com.yas.system.catalog.internal.helper.ProductTemplateHelper;
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
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductTemplateServiceImpl implements ProductTemplateService {

    ProductTemplateRepository productTemplateRepository;
    ProductTemplateHelper productTemplateHelper;

    @Override
    @Transactional
    public void createProductTemplate(ProductTemplateRequest request) {
        validateCreateProductTemplateRequest(request);
        productTemplateRepository.save(productTemplateHelper.createProductTemplate(request));
    }

    @Override
    @Transactional
    public void updateProductTemplate(ProductTemplateRequest request, Integer productTemplateId) {
        validateUpdateProductTemplateRequest(request, productTemplateId);

        ProductTemplate productTemplate = findProductTemplateById(productTemplateId);
        productTemplateHelper.updateProductTemplate(request, productTemplate);
        productTemplateRepository.save(productTemplate);
    }

    @Override
    @Transactional
    public void deleteProductTemplateById(Integer productTemplateId) {
        if (Objects.isNull(productTemplateId)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        ProductTemplate productTemplate = findProductTemplateById(productTemplateId);

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


    private void validateCreateProductTemplateRequest(ProductTemplateRequest request) {
        if (Objects.isNull(request) || isBlank(request.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_TEMPLATE);
        }
        if (productTemplateRepository.checkExited(request.name(), null).isPresent()) {
            throw new InvalidDataException(ErrorCode.PRODUCT_TEMPLATE_ALREADY_EXISTS);
        }
    }

    private void validateUpdateProductTemplateRequest(ProductTemplateRequest request, Integer productTemplateId) {
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


}
