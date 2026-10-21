package com.yas.system.promotion.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.security.annotation.ActiveUser;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.promotion.internal.dto.request.VerifyPromotionRequest;
import com.yas.system.promotion.internal.dto.response.PromotionVerifyResponse;
import com.yas.system.promotion.internal.service.PromotionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionController {

    PromotionService promotionService;

    @PostMapping("/verify")
    public ApiResponse<PromotionVerifyResponse> verifyPromotion(
            @ActiveUser AuthUser customer,
            @Valid @RequestBody VerifyPromotionRequest request
    ) {
        return ApiResponse.success(promotionService.verifyPromotion(customer, request));
    }
}
