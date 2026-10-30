package com.yas.system.catalog.internal.validator;

import com.yas.system.catalog.internal.dto.request.BrandCreateRequest;
import com.yas.system.catalog.internal.dto.request.BrandUpdateRequest;
import com.yas.system.catalog.internal.repository.BrandRepository;
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
public class BrandValidator {

    BrandRepository brandRepository;

    public void validateCreate(BrandCreateRequest request) {
        if (Objects.isNull(request) || isBlank(request.name())) {
            throw new ApplicationException(ErrorCode.INVALID_BRAND);
        }
        if (brandRepository.checkExited(request.name(), null).isPresent()) {
            throw new ApplicationException(ErrorCode.BRAND_ALREADY_EXISTS);
        }
    }

    public void validateUpdate(BrandUpdateRequest request, Integer brandId) {
        validateId(brandId);
        if (Objects.isNull(request) || isBlank(request.name())) {
            throw new ApplicationException(ErrorCode.INVALID_BRAND);
        }
        if (brandRepository.checkExited(request.name(), brandId).isPresent()) {
            throw new ApplicationException(ErrorCode.BRAND_ALREADY_EXISTS);
        }
    }

    public void validateId(Integer brandId) {
        if (Objects.isNull(brandId)) {
            throw new ApplicationException(ErrorCode.INVALID_BRAND);
        }
    }
}

