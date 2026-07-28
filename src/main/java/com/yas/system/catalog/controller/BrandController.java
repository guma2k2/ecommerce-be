package com.yas.system.catalog.controller;

import com.yas.system.catalog.internal.dto.request.BrandCreateRequest;
import com.yas.system.catalog.internal.dto.request.BrandUpdateRequest;
import com.yas.system.catalog.internal.dto.response.BrandResponse;
import com.yas.system.catalog.internal.service.BrandService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/brands")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BrandController {

    BrandService brandService;

    @PostMapping()
    public ApiResponse<Void> createBrand(@RequestBody @Valid BrandCreateRequest request) {
        brandService.createBrand(request);
        return ApiResponse.successWithNoContent();
    }

    @PutMapping("/{brandId}")
    public ApiResponse<Void> updateBrand(
            @RequestBody @Valid BrandUpdateRequest request,
            @PathVariable Long brandId
    ) {
        brandService.updateBrand(request, brandId);
        return ApiResponse.successWithNoContent();
    }

    @GetMapping("/{brandId}")
    public ApiResponse<BrandResponse> getBrand(@PathVariable Long brandId) {
        return ApiResponse.success(brandService.getById(brandId));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<BrandResponse>> getBrandPage(
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(brandService.getBrandPage(pageNumber, pageSize));
    }

    @DeleteMapping("/{brandId}")
    public ApiResponse<Void> deleteBrand(@PathVariable Long brandId) {
        brandService.deleteBrandById(brandId);
        return ApiResponse.successWithNoContent();
    }
}

