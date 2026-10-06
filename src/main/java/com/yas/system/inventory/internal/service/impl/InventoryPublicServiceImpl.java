package com.yas.system.inventory.internal.service.impl;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.inventory.api.InventoryPublicService;
import com.yas.system.inventory.api.dto.InventoryStockDto;
import com.yas.system.inventory.api.dto.StockReservationItemDto;
import com.yas.system.inventory.internal.constant.InventoryConstant;
import com.yas.system.inventory.internal.entity.Inventory;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.enumeration.MovementType;
import com.yas.system.inventory.internal.repository.InventoryRepository;
import com.yas.system.inventory.internal.service.StockMovementService;
import com.yas.system.inventory.internal.service.StockReservationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryPublicServiceImpl implements InventoryPublicService {

    InventoryRepository inventoryRepository;
    StockReservationService stockReservationService;
    StockMovementService stockMovementService;

    @Override
    @Transactional
    public void initializeInventory(Long productVariantId, String sku, int initialStock) {
        if (inventoryRepository.findByProductVariantId(productVariantId).isPresent()) {
            log.info("Inventory already exists for variant ID: {}", productVariantId);
            return;
        }

        Inventory inventory = Inventory.builder()
                .productVariantId(productVariantId)
                .sku(sku)
                .onHand(Math.max(0, initialStock))
                .reserved(0)
                .build();

        Inventory saved = inventoryRepository.save(inventory);

        if (initialStock > 0) {
            stockMovementService.recordMovement(
                    saved.getId(),
                    productVariantId,
                    initialStock,
                    0,
                    initialStock,
                    MovementType.INITIALIZATION,
                    MovementReason.INITIAL_STOCK,
                    null,
                    InventoryConstant.ReasonDefaultNote.INITIAL_STOCK,
                    InventoryConstant.SYSTEM_USER
            );
        }

        log.info("Initialized inventory for variant ID: {} with SKU {} and stock {}",
                productVariantId, sku, initialStock);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStockDto getStock(Long productVariantId) {
        Inventory inventory = inventoryRepository.findByProductVariantId(productVariantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVENTORY_NOT_FOUND, productVariantId));

        return new InventoryStockDto(
                inventory.getProductVariantId(),
                inventory.getSku(),
                inventory.getOnHand(),
                inventory.getReserved(),
                inventory.getAvailable()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, InventoryStockDto> getStockMap(Collection<Long> productVariantIds) {
        if (productVariantIds == null || productVariantIds.isEmpty()) {
            return Map.of();
        }

        List<Inventory> inventories = inventoryRepository.findByProductVariantIdIn(productVariantIds);
        return inventories.stream().collect(Collectors.toMap(
                Inventory::getProductVariantId,
                inv -> new InventoryStockDto(
                        inv.getProductVariantId(),
                        inv.getSku(),
                        inv.getOnHand(),
                        inv.getReserved(),
                        inv.getAvailable()
                )
        ));
    }

    @Override
    @Transactional
    public void reserveStock(String orderId, List<StockReservationItemDto> items, Duration ttl) {
        stockReservationService.reserveStock(orderId, items, ttl);
    }

    @Override
    @Transactional
    public void confirmDeductions(String orderId) {
        stockReservationService.confirmDeductions(orderId);
    }

    @Override
    @Transactional
    public void releaseReservations(String orderId) {
        stockReservationService.releaseReservations(orderId);
    }
}
