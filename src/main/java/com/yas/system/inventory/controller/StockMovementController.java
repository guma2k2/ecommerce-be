package com.yas.system.inventory.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import com.yas.system.inventory.internal.dto.response.StockMovementResponse;
import com.yas.system.inventory.internal.service.StockMovementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventories/movements")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockMovementController {

    StockMovementService stockMovementService;

    @GetMapping("/{variantId}")
    @PreAuthorize("hasAnyAuthority(" +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_SUPERADMIN, " +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_ADMIN)")
    public ApiResponse<PageResponse<StockMovementResponse>> getMovements(
            @PathVariable Long variantId,
            @RequestParam(name = "page_number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize
    ) {
        return ApiResponse.success(stockMovementService.getMovementsByVariantId(variantId, pageNumber, pageSize));
    }
}
