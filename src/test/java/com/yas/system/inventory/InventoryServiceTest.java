package com.yas.system.inventory;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.response.PageResponse;
import com.yas.system.inventory.internal.dto.request.StockAdjustmentRequest;
import com.yas.system.inventory.internal.dto.request.StockCycleCountRequest;
import com.yas.system.inventory.internal.dto.response.InventoryResponse;
import com.yas.system.inventory.internal.entity.Inventory;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.enumeration.MovementType;
import com.yas.system.inventory.internal.helper.InventoryHelper;
import com.yas.system.inventory.internal.repository.InventoryRepository;
import com.yas.system.inventory.internal.service.StockMovementService;
import com.yas.system.inventory.internal.service.impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryService Unit Tests")
class InventoryServiceTest {

    @Mock
    InventoryRepository inventoryRepository;

    @Mock
    StockMovementService stockMovementService;

    InventoryHelper inventoryHelper;
    InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryHelper = new InventoryHelper();
        inventoryService = new InventoryServiceImpl(inventoryRepository, inventoryHelper, stockMovementService);
    }

    @Test
    @DisplayName("getInventories should return paginated inventory responses")
    void getInventories_success() {
        Inventory inv = Inventory.builder()
                .productVariantId(101L)
                .sku("SKU-101")
                .onHand(50)
                .reserved(5)
                .build();
        inv.setId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        when(inventoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(inv), pageable, 1));

        PageResponse<InventoryResponse> result = inventoryService.getInventories(pageable);

        assertThat(result.totalElements()).isEqualTo(1);
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().sku()).isEqualTo("SKU-101");
        assertThat(result.content().getFirst().available()).isEqualTo(45);
    }

    @Test
    @DisplayName("getInventoryByVariantId should return response when found")
    void getInventoryByVariantId_found() {
        Inventory inv = Inventory.builder()
                .productVariantId(102L)
                .sku("SKU-102")
                .onHand(20)
                .reserved(2)
                .build();
        inv.setId(2L);

        when(inventoryRepository.findByProductVariantId(102L)).thenReturn(Optional.of(inv));

        InventoryResponse response = inventoryService.getInventoryByVariantId(102L);

        assertThat(response).isNotNull();
        assertThat(response.productVariantId()).isEqualTo(102L);
        assertThat(response.onHand()).isEqualTo(20);
        assertThat(response.reserved()).isEqualTo(2);
        assertThat(response.available()).isEqualTo(18);
    }

    @Test
    @DisplayName("getInventoryByVariantId should throw ApplicationException when not found")
    void getInventoryByVariantId_notFound_throwsException() {
        when(inventoryRepository.findByProductVariantId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryByVariantId(999L))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getErrorCode()).isEqualTo(ErrorCode.INVENTORY_NOT_FOUND));
    }

    @Test
    @DisplayName("adjustStock should increase on-hand quantity and record stock movement")
    void adjustStock_positiveDelta_success() {
        Inventory invBefore = Inventory.builder().productVariantId(101L).sku("SKU-101").onHand(20).reserved(0).build();
        invBefore.setId(1L);
        Inventory invAfter = Inventory.builder().productVariantId(101L).sku("SKU-101").onHand(30).reserved(0).build();
        invAfter.setId(1L);

        when(inventoryRepository.findByProductVariantId(101L))
                .thenReturn(Optional.of(invBefore))
                .thenReturn(Optional.of(invAfter));
        when(inventoryRepository.adjustOnHand(101L, 10)).thenReturn(1);

        StockAdjustmentRequest request = new StockAdjustmentRequest(101L, 10, MovementReason.RESTOCK, "REF-1", "Restock shipment");
        InventoryResponse response = inventoryService.adjustStock(request, "admin@yas.com");

        assertThat(response.onHand()).isEqualTo(30);
        verify(stockMovementService).recordMovement(
                eq(1L), eq(101L), eq(10), eq(20), eq(30),
                eq(MovementType.ADJUSTMENT), eq(MovementReason.RESTOCK), eq("REF-1"), eq("Restock shipment"), eq("admin@yas.com")
        );
    }

    @Test
    @DisplayName("adjustStock should throw ApplicationException when delta drops onHand below reserved")
    void adjustStock_negativeDeltaExceedsReserved_throwsException() {
        Inventory inv = Inventory.builder().productVariantId(101L).sku("SKU-101").onHand(10).reserved(5).build();
        inv.setId(1L);

        when(inventoryRepository.findByProductVariantId(101L)).thenReturn(Optional.of(inv));

        StockAdjustmentRequest request = new StockAdjustmentRequest(101L, -8, MovementReason.CORRECTION, null, null);

        assertThatThrownBy(() -> inventoryService.adjustStock(request, "admin@yas.com"))
                .isInstanceOf(ApplicationException.class)
                .satisfies(ex -> assertThat(((ApplicationException) ex).getErrorCode()).isEqualTo(ErrorCode.INVALID_STOCK_ADJUSTMENT));

        verify(inventoryRepository, never()).adjustOnHand(any(), anyInt());
    }

    @Test
    @DisplayName("setPhysicalCount should adjust on-hand and record CYCLE_COUNT movement")
    void setPhysicalCount_success() {
        Inventory invBefore = Inventory.builder().productVariantId(101L).sku("SKU-101").onHand(25).reserved(2).build();
        invBefore.setId(1L);
        Inventory invAfter = Inventory.builder().productVariantId(101L).sku("SKU-101").onHand(30).reserved(2).build();
        invAfter.setId(1L);

        when(inventoryRepository.findByProductVariantId(101L))
                .thenReturn(Optional.of(invBefore))
                .thenReturn(Optional.of(invAfter));
        when(inventoryRepository.adjustOnHand(101L, 5)).thenReturn(1);

        StockCycleCountRequest request = new StockCycleCountRequest(101L, 30, "AUDIT-01", "Annual warehouse audit");
        InventoryResponse response = inventoryService.setPhysicalCount(request, "auditor@yas.com");

        assertThat(response.onHand()).isEqualTo(30);
        verify(stockMovementService).recordMovement(
                eq(1L), eq(101L), eq(5), eq(25), eq(30),
                eq(MovementType.CYCLE_COUNT), eq(MovementReason.CYCLE_COUNT_RECONCILIATION),
                eq("AUDIT-01"), eq("Annual warehouse audit"), eq("auditor@yas.com")
        );
    }
}
