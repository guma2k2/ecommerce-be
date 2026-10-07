package com.yas.system.inventory;

import com.yas.system.catalog.AbstractIntegrationTest;
import com.yas.system.inventory.api.InventoryPublicService;
import com.yas.system.inventory.internal.dto.request.StockAdjustmentRequest;
import com.yas.system.inventory.internal.dto.request.StockCycleCountRequest;
import com.yas.system.inventory.internal.entity.Inventory;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.repository.InventoryRepository;
import com.yas.system.inventory.internal.repository.StockMovementRepository;
import com.yas.system.inventory.internal.repository.StockReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Inventory API Integration Tests")
class InventoryApiIT extends AbstractIntegrationTest {

    @Autowired
    InventoryPublicService inventoryPublicService;

    @Autowired
    InventoryRepository inventoryRepository;

    @Autowired
    StockMovementRepository stockMovementRepository;

    @Autowired
    StockReservationRepository stockReservationRepository;

    private Long testVariantId = 9999L;

    @BeforeEach
    void setUp() {
        stockMovementRepository.deleteAll();
        stockReservationRepository.deleteAll();
        inventoryRepository.deleteAll();

        inventoryPublicService.initializeInventory(testVariantId, "TEST-SKU-9999", 50);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @DisplayName("GET /api/v1/inventories/{variantId} should return inventory record")
    void getInventory_success() throws Exception {
        mockMvc.perform(get("/api/v1/inventories/{variantId}", testVariantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("200")))
                .andExpect(jsonPath("$.data.productVariantId", equalTo(testVariantId.intValue())))
                .andExpect(jsonPath("$.data.sku", equalTo("TEST-SKU-9999")))
                .andExpect(jsonPath("$.data.onHand", equalTo(50)))
                .andExpect(jsonPath("$.data.reserved", equalTo(0)))
                .andExpect(jsonPath("$.data.available", equalTo(50)));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @DisplayName("POST /api/v1/inventories/adjust should update onHand and record movement")
    void adjustStock_success() throws Exception {
        StockAdjustmentRequest request = new StockAdjustmentRequest(
                testVariantId,
                15,
                MovementReason.RESTOCK,
                "PO-12345",
                "New inventory delivery"
        );

        mockMvc.perform(post("/api/v1/inventories/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("200")))
                .andExpect(jsonPath("$.data.onHand", equalTo(65)))
                .andExpect(jsonPath("$.data.available", equalTo(65)));

        mockMvc.perform(get("/api/v1/inventories/movements/{variantId}", testVariantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()", greaterThanOrEqualTo(2))) // initial + adjustment
                .andExpect(jsonPath("$.data.content[0].quantityChange", equalTo(15)))
                .andExpect(jsonPath("$.data.content[0].reason", equalTo("RESTOCK")));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    @DisplayName("POST /api/v1/inventories/set-count should update stock to exact physical count")
    void setPhysicalCount_success() throws Exception {
        StockCycleCountRequest request = new StockCycleCountRequest(
                testVariantId,
                42,
                "AUDIT-2026",
                "Monthly count verification"
        );

        mockMvc.perform(post("/api/v1/inventories/set-count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", equalTo("200")))
                .andExpect(jsonPath("$.data.onHand", equalTo(42)))
                .andExpect(jsonPath("$.data.available", equalTo(42)));
    }
}
