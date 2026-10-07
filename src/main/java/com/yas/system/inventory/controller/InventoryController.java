package com.yas.system.inventory.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import com.yas.system.inventory.internal.dto.request.StockAdjustmentRequest;
import com.yas.system.inventory.internal.dto.request.StockCycleCountRequest;
import com.yas.system.inventory.internal.dto.response.InventoryResponse;
import com.yas.system.inventory.internal.service.InventoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {

    InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority(" +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_SUPERADMIN, " +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_ADMIN)")
    public ApiResponse<PageResponse<InventoryResponse>> getInventories(
            @RequestParam(name = "page_number", defaultValue = "0") int pageNumber,
            @RequestParam(name = "page_size", defaultValue = "10") int pageSize
    ) {
        return ApiResponse.success(inventoryService.getInventories(pageNumber, pageSize));
    }

    @GetMapping("/{variantId}")
    @PreAuthorize("hasAnyAuthority(" +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_SUPERADMIN, " +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_ADMIN)")
    public ApiResponse<InventoryResponse> getByVariantId(@PathVariable Long variantId) {
        return ApiResponse.success(inventoryService.getInventoryByVariantId(variantId));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyAuthority(" +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_SUPERADMIN, " +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_ADMIN)")
    public ApiResponse<InventoryResponse> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request
    ) {
        return ApiResponse.success(inventoryService.adjustStock(request));
    }

    @PostMapping("/set-count")
    @PreAuthorize("hasAnyAuthority(" +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_SUPERADMIN, " +
            "T(com.yas.system.common.constant.RoleConstant).ROLE_ADMIN)")
    public ApiResponse<InventoryResponse> setPhysicalCount(
            @Valid @RequestBody StockCycleCountRequest request
    ) {
        return ApiResponse.success(inventoryService.setPhysicalCount(request));
    }
}
