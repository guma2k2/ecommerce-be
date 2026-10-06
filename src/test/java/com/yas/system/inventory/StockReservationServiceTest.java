package com.yas.system.inventory;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.inventory.api.dto.StockReservationItemDto;
import com.yas.system.inventory.internal.entity.Inventory;
import com.yas.system.inventory.internal.entity.StockReservation;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.enumeration.MovementType;
import com.yas.system.inventory.internal.enumeration.ReservationStatus;
import com.yas.system.inventory.internal.repository.InventoryRepository;
import com.yas.system.inventory.internal.repository.StockReservationRepository;
import com.yas.system.inventory.internal.service.StockMovementService;
import com.yas.system.inventory.internal.service.impl.StockReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockReservationService Unit Tests")
class StockReservationServiceTest {

    @Mock
    InventoryRepository inventoryRepository;

    @Mock
    StockReservationRepository stockReservationRepository;

    @Mock
    StockMovementService stockMovementService;

    StockReservationServiceImpl stockReservationService;

    final String orderId = "00000000-0000-0000-0000-000000000099";

    @BeforeEach
    void setUp() {
        stockReservationService = new StockReservationServiceImpl(
                inventoryRepository,
                stockReservationRepository,
                stockMovementService
        );
    }

    @Test
    @DisplayName("reserveStock should atomically reserve stock and persist PENDING reservations")
    void reserveStock_success() {
        when(stockReservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.PENDING))
                .thenReturn(List.of());
        when(inventoryRepository.reserveStock(101L, 2)).thenReturn(1);

        List<StockReservationItemDto> items = List.of(new StockReservationItemDto(101L, 2));
        stockReservationService.reserveStock(orderId, items, Duration.ofMinutes(15));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StockReservation>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockReservationRepository).saveAll(captor.capture());

        List<StockReservation> savedList = captor.getValue();
        assertThat(savedList).hasSize(1);
        StockReservation saved = savedList.getFirst();
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getProductVariantId()).isEqualTo(101L);
        assertThat(saved.getReservedQuantity()).isEqualTo(2);
        assertThat(saved.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("reserveStock should throw INSUFFICIENT_STOCK when atomic reservation fails")
    void reserveStock_insufficientStock_throwsException() {
        when(stockReservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.PENDING))
                .thenReturn(List.of());
        when(inventoryRepository.reserveStock(101L, 5)).thenReturn(0);

        Inventory inv = Inventory.builder().productVariantId(101L).onHand(10).reserved(8).build();
        when(inventoryRepository.findByProductVariantId(101L)).thenReturn(Optional.of(inv));

        List<StockReservationItemDto> items = List.of(new StockReservationItemDto(101L, 5));

        assertThatThrownBy(() -> stockReservationService.reserveStock(orderId, items, Duration.ofMinutes(15)))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getErrorCode()).isEqualTo(ErrorCode.INSUFFICIENT_STOCK));

        verify(stockReservationRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("confirmDeductions should deduct physical stock and mark reservation CONFIRMED")
    void confirmDeductions_success() {
        StockReservation res = StockReservation.builder()
                .orderId(orderId)
                .productVariantId(101L)
                .reservedQuantity(3)
                .status(ReservationStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(600))
                .build();

        when(stockReservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.PENDING))
                .thenReturn(List.of(res));
        when(inventoryRepository.confirmDeduction(101L, 3)).thenReturn(1);

        Inventory inv = Inventory.builder().productVariantId(101L).onHand(17).reserved(0).build();
        inv.setId(1L);
        when(inventoryRepository.findByProductVariantId(101L)).thenReturn(Optional.of(inv));

        stockReservationService.confirmDeductions(orderId);

        assertThat(res.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(inventoryRepository).confirmDeduction(101L, 3);
        verify(stockMovementService).recordMovement(
                eq(1L), eq(101L), eq(-3), eq(20), eq(17),
                eq(MovementType.ORDER_DEDUCTION), eq(MovementReason.ORDER_CHECKOUT),
                eq(orderId), any(), any()
        );
    }

    @Test
    @DisplayName("releaseReservations should release reserved stock and mark reservation RELEASED")
    void releaseReservations_success() {
        StockReservation res = StockReservation.builder()
                .orderId(orderId)
                .productVariantId(101L)
                .reservedQuantity(4)
                .status(ReservationStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(600))
                .build();

        when(stockReservationRepository.findByOrderIdAndStatus(orderId, ReservationStatus.PENDING))
                .thenReturn(List.of(res));
        when(inventoryRepository.releaseReservation(101L, 4)).thenReturn(1);

        Inventory inv = Inventory.builder().productVariantId(101L).onHand(20).reserved(0).build();
        inv.setId(1L);
        when(inventoryRepository.findByProductVariantId(101L)).thenReturn(Optional.of(inv));

        stockReservationService.releaseReservations(orderId);

        assertThat(res.getStatus()).isEqualTo(ReservationStatus.RELEASED);
        verify(inventoryRepository).releaseReservation(101L, 4);
        verify(stockMovementService).recordMovement(
                eq(1L), eq(101L), eq(0), eq(20), eq(20),
                eq(MovementType.RESERVATION_RELEASE), eq(MovementReason.ORDER_CANCELLED),
                eq(orderId), any(), any()
        );
    }

    @Test
    @DisplayName("releaseExpiredReservations should release reservations past their expiration timestamp")
    void releaseExpiredReservations_success() {
        StockReservation res = StockReservation.builder()
                .orderId(orderId)
                .productVariantId(101L)
                .reservedQuantity(2)
                .status(ReservationStatus.PENDING)
                .expiresAt(Instant.now().minusSeconds(100))
                .build();

        when(stockReservationRepository.findByStatusAndExpiresAtBefore(eq(ReservationStatus.PENDING), any()))
                .thenReturn(List.of(res));

        stockReservationService.releaseExpiredReservations();

        assertThat(res.getStatus()).isEqualTo(ReservationStatus.EXPIRED);
        verify(inventoryRepository).releaseReservation(101L, 2);
    }
}
