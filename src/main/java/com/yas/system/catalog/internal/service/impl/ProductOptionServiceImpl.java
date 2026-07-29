package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.ProductOptionCreateRequest;
import com.yas.system.catalog.internal.dto.request.ProductOptionUpdateRequest;
import com.yas.system.catalog.internal.dto.response.ProductOptionResponse;
import com.yas.system.catalog.internal.entity.option.ProductOption;
import com.yas.system.catalog.internal.helper.ProductOptionHelper;
import com.yas.system.catalog.internal.repository.ProductOptionRepository;
import com.yas.system.catalog.internal.service.ProductOptionService;
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

import java.util.List;
import java.util.Objects;

import static com.yas.system.common.util.StringUtils.isBlank;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductOptionServiceImpl implements ProductOptionService {

    ProductOptionRepository productOptionRepository;
    ProductOptionHelper productOptionHelper;

    @Override
    @Transactional
    public void createProductOption(ProductOptionCreateRequest request) {
        validateCreateProductOptionRequest(request);
        productOptionRepository.save(productOptionHelper.createProductOption(request));
    }

    @Override
    @Transactional
    public void updateProductOption(ProductOptionUpdateRequest request, Long productOptionId) {
        validateUpdateProductOptionRequest(request, productOptionId);

        ProductOption productOption = findProductOptionById(productOptionId);
        productOptionHelper.updateProductOption(request, productOption);
        productOptionRepository.save(productOption);
    }

    @Override
    @Transactional
    public void deleteProductOptionById(Long productOptionId) {
        if (Objects.isNull(productOptionId)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_OPTION);
        }
        ProductOption productOption = findProductOptionById(productOptionId);
        productOptionRepository.delete(productOption);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductOptionResponse getById(Long productOptionId) {
        if (Objects.isNull(productOptionId)) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_OPTION);
        }
        return ProductOptionResponse.from(findProductOptionById(productOptionId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductOptionResponse> getProductOptionPage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<ProductOption> productOptionPage = productOptionRepository.findAll(pageable);

        List<ProductOptionResponse> content = productOptionPage.getContent().stream()
                .map(ProductOptionResponse::from)
                .toList();

        return new PageResponse<>(
                productOptionPage.getNumber(),
                productOptionPage.getSize(),
                productOptionPage.getTotalPages(),
                productOptionPage.getTotalElements(),
                content
        );
    }

    private void validateCreateProductOptionRequest(ProductOptionCreateRequest request) {
        if (Objects.isNull(request) || isBlank(request.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_OPTION);
        }
        if (productOptionRepository.checkExited(request.name(), null).isPresent()) {
            throw new InvalidDataException(ErrorCode.PRODUCT_OPTION_ALREADY_EXISTS);
        }
    }

    private void validateUpdateProductOptionRequest(ProductOptionUpdateRequest request, Long productOptionId) {
        if (Objects.isNull(productOptionId) || Objects.isNull(request) || isBlank(request.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_PRODUCT_OPTION);
        }
        if (productOptionRepository.checkExited(request.name(), productOptionId).isPresent()) {
            throw new InvalidDataException(ErrorCode.PRODUCT_OPTION_ALREADY_EXISTS);
        }
    }

    private ProductOption findProductOptionById(Long productOptionId) {
        return productOptionRepository.findById(productOptionId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));
    }
}
