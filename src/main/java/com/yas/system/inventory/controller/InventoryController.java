package com.yas.system.inventory.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import com.yas.system.common.security.annotation.ActiveUser;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.inventory.internal.dto.request.StockAdjustmentRequest;
import com.yas.system.inventory.internal.dto.request.StockCycleCountRequest;
import com.yas.system.inventory.internal.dto.response.InventoryResponse;
import com.yas.system.inventory.internal.service.InventoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {

    InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'INVENTORY_VIEW')")
    public ApiResponse<PageResponse<InventoryResponse>> getInventories(
            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ApiResponse.success(inventoryService.getInventories(pageable));
    }

    @GetMapping("/{variantId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'INVENTORY_VIEW')")
    public ApiResponse<InventoryResponse> getByVariantId(@PathVariable Long variantId) {
        return ApiResponse.success(inventoryService.getInventoryByVariantId(variantId));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'INVENTORY_UPDATE')")
    public ApiResponse<InventoryResponse> adjustStock(
            @Valid @RequestBody StockAdjustmentRequest request,
            @ActiveUser AuthUser user
    ) {
        String performer = resolvePerformer(user);
        return ApiResponse.success(inventoryService.adjustStock(request, performer));
    }

    @PostMapping("/set-count")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'STAFF', 'INVENTORY_UPDATE')")
    public ApiResponse<InventoryResponse> setPhysicalCount(
            @Valid @RequestBody StockCycleCountRequest request,
            @ActiveUser AuthUser user
    ) {
        String performer = resolvePerformer(user);
        return ApiResponse.success(inventoryService.setPhysicalCount(request, performer));
    }

    private String resolvePerformer(AuthUser user) {
        if (user != null && user.email() != null) {
            return user.email();
        }
        return "ADMIN";
    }
}
