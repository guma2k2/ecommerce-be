package com.yas.system.promotion.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import com.yas.system.promotion.internal.constant.PromotionConstant;
import com.yas.system.promotion.internal.dto.request.CreatePromotionRequest;
import com.yas.system.promotion.internal.dto.request.UpdatePromotionRequest;
import com.yas.system.promotion.internal.dto.response.PromotionDetailResponse;
import com.yas.system.promotion.internal.dto.response.PromotionResponse;
import com.yas.system.promotion.service.PromotionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;

@RestController
@RequestMapping("/api/v1/promotions/backoffice")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//@PreAuthorize("hasAnyAuthority(T(com.yas.system.common.constant.RoleConstant).ROLE_ADMIN)")
public class AdminPromotionController {

    PromotionService promotionService;

    @GetMapping
    public ApiResponse<PageResponse<PromotionResponse>> getPromotions(
            @RequestParam(name = "page_number", defaultValue = PromotionConstant.DEFAULT_PAGE_NUMBER) int pageNumber,
            @RequestParam(name = "page_size", defaultValue = PromotionConstant.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(name = "coupon_code", required = false) String couponCode,
            @RequestParam(name = "is_active", required = false) Boolean isActive,
            @RequestParam(name = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime startDate,
            @RequestParam(name = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime endDate
    ) {
        return ApiResponse.success(promotionService.getPromotions(
                pageNumber,
                pageSize,
                name,
                couponCode,
                isActive,
                startDate,
                endDate
        ));
    }

    @GetMapping("/{id}")
    public ApiResponse<PromotionDetailResponse> getPromotionById(@PathVariable Long id) {
        return ApiResponse.success(promotionService.getPromotionById(id));
    }

    @PostMapping
    public ApiResponse<PromotionDetailResponse> createPromotion(@Valid @RequestBody CreatePromotionRequest request) {
        return ApiResponse.success(promotionService.createPromotion(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<PromotionDetailResponse> updatePromotion(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePromotionRequest request
    ) {
        return ApiResponse.success(promotionService.updatePromotion(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePromotion(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ApiResponse.successWithNoContent();
    }
}
