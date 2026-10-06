package com.yas.system.inventory.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import com.yas.system.inventory.internal.dto.response.StockMovementResponse;
import com.yas.system.inventory.internal.service.StockMovementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventories/movements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockMovementController {

    StockMovementService stockMovementService;

    @GetMapping("/{variantId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'INVENTORY_VIEW')")
    public ApiResponse<PageResponse<StockMovementResponse>> getMovements(
            @PathVariable Long variantId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ApiResponse.success(stockMovementService.getMovementsByVariantId(variantId, pageable));
    }
}
