package com.yas.system.inventory.internal.helper;

import com.yas.system.inventory.internal.entity.Inventory;
import com.yas.system.inventory.internal.validator.InventoryValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryHelper {

    InventoryValidator inventoryValidator;

    public void validateAdjustment(Inventory inventory, int delta) {
        inventoryValidator.validateAdjustment(inventory, delta);
    }

    public void validateCycleCount(Inventory inventory, int physicalCount) {
        inventoryValidator.validateCycleCount(inventory, physicalCount);
    }
}
