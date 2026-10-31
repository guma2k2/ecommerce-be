package com.yas.system.catalog.service.impl;

import com.yas.system.catalog.internal.dto.request.BrandCreateRequest;
import com.yas.system.catalog.internal.dto.request.BrandUpdateRequest;
import com.yas.system.catalog.internal.dto.response.BrandResponse;
import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.helper.BrandHelper;
import com.yas.system.catalog.internal.repository.BrandRepository;
import com.yas.system.catalog.service.BrandService;
import com.yas.system.catalog.internal.validator.BrandValidator;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BrandServiceImpl implements BrandService {

    BrandRepository brandRepository;
    BrandHelper brandHelper;
    BrandValidator brandValidator;

    @Override
    @Transactional
    public BrandResponse createBrand(BrandCreateRequest request) {
        brandValidator.validateCreate(request);
        Brand brand = brandRepository.save(brandHelper.createBrand(request));
        return BrandResponse.from(brand);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(BrandUpdateRequest request, Integer brandId) {
        brandValidator.validateUpdate(request, brandId);

        Brand brand = findBrandById(brandId);
        brandHelper.updateBrand(request, brand);
        Brand savedBrand = brandRepository.save(brand);
        return BrandResponse.from(savedBrand);
    }

    @Override
    @Transactional
    public void deleteBrandById(Integer brandId) {
        brandValidator.validateId(brandId);
        Brand brand = findBrandById(brandId);

        brandRepository.delete(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getById(Integer brandId) {
        brandValidator.validateId(brandId);
        return BrandResponse.from(findBrandById(brandId));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BrandResponse> getBrandPage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Brand> brandPage = brandRepository.findAll(pageable);

        List<BrandResponse> content = brandPage.getContent().stream()
                .map(BrandResponse::from)
                .toList();

        return new PageResponse<>(
                brandPage.getNumber(),
                brandPage.getSize(),
                brandPage.getTotalPages(),
                brandPage.getTotalElements(),
                content
        );
    }

    private Brand findBrandById(Integer brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.BRAND_NOT_FOUND));
    }
}
