package com.yas.system.inventory.service;

import com.yas.system.common.response.PageResponse;
import com.yas.system.inventory.api.dto.StockReservationItemDto;
import com.yas.system.inventory.internal.dto.request.StockAdjustmentRequest;
import com.yas.system.inventory.internal.dto.request.StockCycleCountRequest;
import com.yas.system.inventory.internal.dto.response.InventoryResponse;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.List;

public interface InventoryService {

    Duration DEFAULT_RESERVATION_TTL = Duration.ofMinutes(15);

    PageResponse<InventoryResponse> getInventories(int pageNumber, int pageSize);

    PageResponse<InventoryResponse> getInventories(Pageable pageable);

    InventoryResponse getInventoryByVariantId(Long variantId);

    InventoryResponse adjustStock(StockAdjustmentRequest request);

    InventoryResponse adjustStock(StockAdjustmentRequest request, String performedBy);

    InventoryResponse setPhysicalCount(StockCycleCountRequest request);

    InventoryResponse setPhysicalCount(StockCycleCountRequest request, String performedBy);

    void initializeInventory(Long productVariantId, String sku, int initialStock);

    void reserveStock(String orderId, List<StockReservationItemDto> items, Duration ttl);

    default void reserveStock(String orderId, List<StockReservationItemDto> items) {
        reserveStock(orderId, items, DEFAULT_RESERVATION_TTL);
    }

    void confirmDeductions(String orderId);

    void releaseReservations(String orderId);
}
