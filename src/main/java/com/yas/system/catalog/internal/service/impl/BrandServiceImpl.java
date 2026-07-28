package com.yas.system.catalog.internal.service.impl;

import com.yas.system.catalog.internal.dto.request.BrandCreateRequest;
import com.yas.system.catalog.internal.dto.request.BrandUpdateRequest;
import com.yas.system.catalog.internal.dto.response.BrandResponse;
import com.yas.system.catalog.internal.entity.Brand;
import com.yas.system.catalog.internal.helper.BrandHelper;
import com.yas.system.catalog.internal.repository.BrandRepository;
import com.yas.system.catalog.internal.service.BrandService;
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
public class BrandServiceImpl implements BrandService {

    BrandRepository brandRepository;
    BrandHelper brandHelper;

    @Override
    @Transactional
    public void createBrand(BrandCreateRequest request) {
        validateCreateBrandRequest(request);
        brandRepository.save(brandHelper.createBrand(request));
    }

    @Override
    @Transactional
    public void updateBrand(BrandUpdateRequest request, Long brandId) {
        validateUpdateBrandRequest(request, brandId);

        Brand brand = findBrandById(brandId);
        brandHelper.updateBrand(request, brand);
        brandRepository.save(brand);
    }

    @Override
    @Transactional
    public void deleteBrandById(Long brandId) {
        if (Objects.isNull(brandId)) {
            throw new InvalidDataException(ErrorCode.INVALID_BRAND);
        }
        Brand brand = findBrandById(brandId);

        brandRepository.delete(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponse getById(Long brandId) {
        if (Objects.isNull(brandId)) {
            throw new InvalidDataException(ErrorCode.INVALID_BRAND);
        }
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


    private void validateCreateBrandRequest(BrandCreateRequest request) {
        if (Objects.isNull(request) || isBlank(request.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_BRAND);
        }
        if (brandRepository.checkExited(request.name(), null).isPresent()) {
            throw new InvalidDataException(ErrorCode.BRAND_ALREADY_EXISTS);
        }
    }

    private void validateUpdateBrandRequest(BrandUpdateRequest request, Long brandId) {
        if (Objects.isNull(brandId) || Objects.isNull(request) || isBlank(request.name())) {
            throw new InvalidDataException(ErrorCode.INVALID_BRAND);
        }
        if (brandRepository.checkExited(request.name(), brandId).isPresent()) {
            throw new InvalidDataException(ErrorCode.BRAND_ALREADY_EXISTS);
        }
    }

    private Brand findBrandById(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRAND_NOT_FOUND));
    }


}
