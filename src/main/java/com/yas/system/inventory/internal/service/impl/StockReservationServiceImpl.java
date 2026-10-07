package com.yas.system.inventory.internal.service.impl;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.inventory.api.dto.StockReservationItemDto;
import com.yas.system.inventory.internal.constant.InventoryConstant;
import com.yas.system.inventory.internal.entity.Inventory;
import com.yas.system.inventory.internal.entity.StockReservation;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.enumeration.MovementType;
import com.yas.system.inventory.internal.enumeration.ReservationStatus;
import com.yas.system.inventory.internal.repository.InventoryRepository;
import com.yas.system.inventory.internal.repository.StockReservationRepository;
import com.yas.system.inventory.internal.service.StockMovementService;
import com.yas.system.inventory.internal.service.StockReservationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StockReservationServiceImpl implements StockReservationService {

    InventoryRepository inventoryRepository;
    StockReservationRepository stockReservationRepository;
    StockMovementService stockMovementService;

    @Override
    @Transactional
    public void reserveStock(String orderId, List<StockReservationItemDto> items, Duration ttl) {
        if (items == null || items.isEmpty()) {
            return;
        }

        // Check if pending reservations already exist for this order
        List<StockReservation> existing = stockReservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.PENDING);
        if (!existing.isEmpty()) {
            log.info("Active reservation already exists for order ID: {}", orderId);
            return;
        }

        Instant expiresAt = Instant.now().plus(ttl != null ? ttl : InventoryConstant.DEFAULT_RESERVATION_TTL);
        List<StockReservation> reservationsToSave = new ArrayList<>();

        for (StockReservationItemDto item : items) {
            Long variantId = item.productVariantId();
            int qty = item.quantity();

            int updatedRows = inventoryRepository.reserveStock(variantId, qty);
            if (updatedRows == 0) {
                Inventory inventory = inventoryRepository.findByProductVariantId(variantId)
                        .orElseThrow(() -> new ApplicationException(ErrorCode.INVENTORY_NOT_FOUND, variantId));
                throw new ApplicationException(ErrorCode.INSUFFICIENT_STOCK, qty, inventory.getAvailable());
            }

            reservationsToSave.add(StockReservation.builder()
                    .orderId(orderId)
                    .productVariantId(variantId)
                    .reservedQuantity(qty)
                    .status(ReservationStatus.PENDING)
                    .expiresAt(expiresAt)
                    .build());
        }

        stockReservationRepository.saveAll(reservationsToSave);
        log.info("Reserved stock for order ID: {} with {} items until {}", orderId, items.size(), expiresAt);
    }

    @Override
    @Transactional
    public void confirmDeductions(String orderId) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.PENDING);
        if (reservations.isEmpty()) {
            List<StockReservation> confirmed = stockReservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.CONFIRMED);
            if (!confirmed.isEmpty()) {
                log.info("Reservation for order {} is already confirmed", orderId);
                return;
            }
            log.warn("No pending reservation found to confirm for order {}", orderId);
            return;
        }

        for (StockReservation res : reservations) {
            inventoryRepository.confirmDeduction(res.getProductVariantId(), res.getReservedQuantity());
            res.setStatus(ReservationStatus.CONFIRMED);

            inventoryRepository.findByProductVariantId(res.getProductVariantId()).ifPresent(inv ->
                    stockMovementService.recordMovement(
                            inv.getId(),
                            res.getProductVariantId(),
                            -res.getReservedQuantity(),
                            inv.getOnHand() + res.getReservedQuantity(),
                            inv.getOnHand(),
                            MovementType.ORDER_DEDUCTION,
                            MovementReason.ORDER_CHECKOUT,
                            orderId,
                            InventoryConstant.ReasonDefaultNote.ORDER_DEDUCTION,
                            InventoryConstant.SYSTEM_USER
                    )
            );
        }

        stockReservationRepository.saveAll(reservations);
        log.info("Confirmed stock deduction for order ID: {}", orderId);
    }

    @Override
    @Transactional
    public void releaseReservations(String orderId) {
        List<StockReservation> reservations = stockReservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.PENDING);
        if (reservations.isEmpty()) {
            log.info("No pending reservation found to release for order {}", orderId);
            return;
        }

        for (StockReservation res : reservations) {
            inventoryRepository.releaseReservation(res.getProductVariantId(), res.getReservedQuantity());
            res.setStatus(ReservationStatus.RELEASED);

            inventoryRepository.findByProductVariantId(res.getProductVariantId()).ifPresent(inv ->
                    stockMovementService.recordMovement(
                            inv.getId(),
                            res.getProductVariantId(),
                            0, // On-hand unchanged; only reserved decreased
                            inv.getOnHand(),
                            inv.getOnHand(),
                            MovementType.RESERVATION_RELEASE,
                            MovementReason.ORDER_CANCELLED,
                            orderId,
                            InventoryConstant.ReasonDefaultNote.ORDER_CANCELLATION,
                            InventoryConstant.SYSTEM_USER
                    )
            );
        }

        stockReservationRepository.saveAll(reservations);
        log.info("Released stock reservation for order ID: {}", orderId);
    }

    @Override
    @Transactional
    public void releaseExpiredReservations() {
        Instant now = Instant.now();
        List<StockReservation> expiredList = stockReservationRepository.findByStatusAndExpiresAtBefore(ReservationStatus.PENDING, now);
        if (expiredList.isEmpty()) {
            return;
        }

        log.info("Found {} expired stock reservations to release", expiredList.size());
        for (StockReservation res : expiredList) {
            inventoryRepository.releaseReservation(res.getProductVariantId(), res.getReservedQuantity());
            res.setStatus(ReservationStatus.EXPIRED);
        }
        stockReservationRepository.saveAll(expiredList);
    }
}
