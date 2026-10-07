package com.yas.system.inventory.internal.service;

import com.yas.system.common.response.PageResponse;
import com.yas.system.inventory.internal.dto.request.StockAdjustmentRequest;
import com.yas.system.inventory.internal.dto.request.StockCycleCountRequest;
import com.yas.system.inventory.internal.dto.response.InventoryResponse;
import org.springframework.data.domain.Pageable;

public interface InventoryService {

    PageResponse<InventoryResponse> getInventories(int pageNumber, int pageSize);

    PageResponse<InventoryResponse> getInventories(Pageable pageable);

    InventoryResponse getInventoryByVariantId(Long variantId);

    InventoryResponse adjustStock(StockAdjustmentRequest request);

    InventoryResponse adjustStock(StockAdjustmentRequest request, String performedBy);

    InventoryResponse setPhysicalCount(StockCycleCountRequest request);

    InventoryResponse setPhysicalCount(StockCycleCountRequest request, String performedBy);
}
