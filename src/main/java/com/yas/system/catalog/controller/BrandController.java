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
    public ApiResponse<BrandResponse> createBrand(@RequestBody @Valid BrandCreateRequest request) {
        return ApiResponse.success(brandService.createBrand(request));
    }

    @PutMapping("/{brandId}")
    public ApiResponse<BrandResponse> updateBrand(
            @RequestBody @Valid BrandUpdateRequest request,
            @PathVariable Integer brandId
    ) {
        return ApiResponse.success(brandService.updateBrand(request, brandId));
    }

    @GetMapping("/{brandId}")
    public ApiResponse<BrandResponse> getBrand(@PathVariable Integer brandId) {
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
    public ApiResponse<Void> deleteBrand(@PathVariable Integer brandId) {
        brandService.deleteBrandById(brandId);
        return ApiResponse.successWithNoContent();
    }
}

