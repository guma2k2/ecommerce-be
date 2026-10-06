package com.yas.system.inventory.api;

import com.yas.system.inventory.api.dto.InventoryStockDto;
import com.yas.system.inventory.api.dto.StockReservationItemDto;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Public contract exposed by the Inventory module for cross-module interactions.
 */
public interface InventoryPublicService {

    /**
     * Initializes an inventory ledger for a newly created product variant.
     *
     * @param productVariantId Variant identifier
     * @param sku Variant SKU
     * @param initialStock Initial on-hand physical stock
     */
    void initializeInventory(Long productVariantId, String sku, int initialStock);

    /**
     * Retrieves stock state for a specific product variant.
     *
     * @param productVariantId Variant identifier
     * @return InventoryStockDto
     */
    InventoryStockDto getStock(Long productVariantId);

    /**
     * Retrieves stock state for a collection of product variants.
     *
     * @param productVariantIds Collection of variant identifiers
     * @return Map of variantId -> InventoryStockDto
     */
    Map<Long, InventoryStockDto> getStockMap(Collection<Long> productVariantIds);

    /**
     * Atomically reserves stock for an order before payment.
     *
     * @param orderId Associated order identifier
     * @param items Items and quantities to reserve
     * @param ttl Time-to-live before reservation expires
     */
    void reserveStock(String orderId, List<StockReservationItemDto> items, Duration ttl);

    /**
     * Confirms and finalizes stock deduction upon successful checkout/payment.
     * Decrements both onHand and reserved stock, logging a permanent stock movement.
     *
     * @param orderId Associated order identifier
     */
    void confirmDeductions(String orderId);

    /**
     * Releases active stock reservations upon order cancellation or checkout expiration.
     *
     * @param orderId Associated order identifier
     */
    void releaseReservations(String orderId);
}
