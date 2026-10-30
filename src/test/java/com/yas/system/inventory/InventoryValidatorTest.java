package com.yas.system.inventory;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.inventory.internal.entity.Inventory;
import com.yas.system.inventory.internal.validator.InventoryValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryValidatorTest {

    private InventoryValidator inventoryValidator;

    @BeforeEach
    void setUp() {
        inventoryValidator = new InventoryValidator();
    }

    @Test
    @DisplayName("validateAdjustment throws INVALID_STOCK_ADJUSTMENT when new quantity is negative")
    void validateAdjustment_negativeResult() {
        Inventory inventory = Inventory.builder()
                .onHand(5)
                .reserved(0)
                .build();

        assertThatThrownBy(() -> inventoryValidator.validateAdjustment(inventory, -10))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STOCK_ADJUSTMENT);
    }

    @Test
    @DisplayName("validateAdjustment throws INVALID_STOCK_ADJUSTMENT when new quantity is less than reserved")
    void validateAdjustment_belowReserved() {
        Inventory inventory = Inventory.builder()
                .onHand(10)
                .reserved(5)
                .build();

        assertThatThrownBy(() -> inventoryValidator.validateAdjustment(inventory, -6))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STOCK_ADJUSTMENT);
    }

    @Test
    @DisplayName("validateAdjustment succeeds when new quantity >= reserved and non-negative")
    void validateAdjustment_success() {
        Inventory inventory = Inventory.builder()
                .onHand(10)
                .reserved(3)
                .build();

        assertThatCode(() -> inventoryValidator.validateAdjustment(inventory, -4))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateCycleCount throws INVALID_STOCK_ADJUSTMENT when physicalCount is negative")
    void validateCycleCount_negative() {
        Inventory inventory = Inventory.builder()
                .onHand(10)
                .reserved(2)
                .build();

        assertThatThrownBy(() -> inventoryValidator.validateCycleCount(inventory, -1))
                .isInstanceOf(ApplicationException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_STOCK_ADJUSTMENT);
    }
}
