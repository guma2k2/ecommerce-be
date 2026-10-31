package com.yas.system.inventory.internal.service.impl;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.response.PageResponse;
import com.yas.system.inventory.internal.constant.InventoryConstant;
import com.yas.system.inventory.internal.dto.request.StockAdjustmentRequest;
import com.yas.system.inventory.internal.dto.request.StockCycleCountRequest;
import com.yas.system.inventory.internal.dto.response.InventoryResponse;
import com.yas.system.inventory.internal.entity.Inventory;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.enumeration.MovementType;
import com.yas.system.inventory.internal.helper.InventoryHelper;
import com.yas.system.inventory.internal.repository.InventoryRepository;
import com.yas.system.inventory.api.dto.StockReservationItemDto;
import com.yas.system.inventory.service.InventoryService;
import com.yas.system.inventory.service.StockReservationService;
import java.time.Duration;
import com.yas.system.inventory.service.StockMovementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryServiceImpl implements InventoryService {

    InventoryRepository inventoryRepository;
    InventoryHelper inventoryHelper;
    StockMovementService stockMovementService;
    StockReservationService stockReservationService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> getInventories(int pageNumber, int pageSize) {
        return getInventories(PageRequest.of(pageNumber, pageSize));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryResponse> getInventories(Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findAll(pageable);
        List<InventoryResponse> content = page.getContent().stream()
                .map(InventoryResponse::from)
                .toList();

        return new PageResponse<>(
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements(),
                content
        );
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByVariantId(Long variantId) {
        Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVENTORY_NOT_FOUND, variantId));
        return InventoryResponse.from(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock(StockAdjustmentRequest request) {
        return adjustStock(request, null);
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock(StockAdjustmentRequest request, String performedBy) {
        Long variantId = request.productVariantId();
        Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVENTORY_NOT_FOUND, variantId));

        int delta = request.quantityChange();
        inventoryHelper.validateAdjustment(inventory, delta);

        int beforeOnHand = inventory.getOnHand();
        int updatedRows = inventoryRepository.adjustOnHand(variantId, delta);
        if (updatedRows == 0) {
            throw new ApplicationException(ErrorCode.INVALID_STOCK_ADJUSTMENT, "Concurrent update conflict during stock adjustment");
        }

        Inventory updated = inventoryRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVENTORY_NOT_FOUND, variantId));

        MovementReason reason = request.reason() != null
                ? request.reason()
                : (delta >= 0 ? MovementReason.RESTOCK : MovementReason.CORRECTION);

        stockMovementService.recordMovement(
                updated.getId(),
                variantId,
                delta,
                beforeOnHand,
                updated.getOnHand(),
                MovementType.ADJUSTMENT,
                reason,
                request.referenceId(),
                request.note(),
                performedBy
        );

        log.info("Adjusted stock for variant {}: delta={}, before={}, after={}, performedBy={}",
                variantId, delta, beforeOnHand, updated.getOnHand(), performedBy);

        return InventoryResponse.from(updated);
    }

    @Override
    @Transactional
    public InventoryResponse setPhysicalCount(StockCycleCountRequest request) {
        return setPhysicalCount(request, null);
    }

    @Override
    @Transactional
    public InventoryResponse setPhysicalCount(StockCycleCountRequest request, String performedBy) {
        Long variantId = request.productVariantId();
        Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVENTORY_NOT_FOUND, variantId));

        int physicalCount = request.physicalCount();
        inventoryHelper.validateCycleCount(inventory, physicalCount);

        int beforeOnHand = inventory.getOnHand();
        int delta = physicalCount - beforeOnHand;

        int updatedRows = inventoryRepository.adjustOnHand(variantId, delta);
        if (updatedRows == 0) {
            throw new ApplicationException(ErrorCode.INVALID_STOCK_ADJUSTMENT, "Concurrent update conflict during cycle count");
        }

        Inventory updated = inventoryRepository.findByProductVariantId(variantId)
                .orElseThrow(() -> new ApplicationException(ErrorCode.INVENTORY_NOT_FOUND, variantId));

        String note = request.note() != null ? request.note() : InventoryConstant.ReasonDefaultNote.CYCLE_COUNT;
        stockMovementService.recordMovement(
                updated.getId(),
                variantId,
                delta,
                beforeOnHand,
                updated.getOnHand(),
                MovementType.CYCLE_COUNT,
                MovementReason.CYCLE_COUNT_RECONCILIATION,
                request.referenceId(),
                note,
                performedBy
        );

        log.info("Set physical stock count for variant {}: count={}, delta={}, performedBy={}",
                variantId, physicalCount, delta, performedBy);

        return InventoryResponse.from(updated);
    }

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
