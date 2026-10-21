package com.yas.system.promotion.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import com.yas.system.promotion.internal.dto.request.CreatePromotionRequest;
import com.yas.system.promotion.internal.dto.request.UpdatePromotionRequest;
import com.yas.system.promotion.internal.dto.response.PromotionDetailResponse;
import com.yas.system.promotion.internal.dto.response.PromotionResponse;
import com.yas.system.promotion.internal.service.PromotionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
@RequestMapping("/api/v1/backoffice/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromotionController {

    PromotionService promotionService;

    @GetMapping
    public ApiResponse<PageResponse<PromotionResponse>> getPromotions(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) ZonedDateTime startDate,
            @RequestParam(required = false) ZonedDateTime endDate
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
