package com.yas.system.inventory.internal.helper;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.inventory.internal.entity.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryHelper {

    public void validateAdjustment(Inventory inventory, int delta) {
        int newOnHand = inventory.getOnHand() + delta;
        if (newOnHand < 0) {
            throw new ApplicationException(ErrorCode.INVALID_STOCK_ADJUSTMENT,
                    String.format("Resulting on-hand quantity (%d) cannot be negative", newOnHand));
        }
        if (newOnHand < inventory.getReserved()) {
            throw new ApplicationException(ErrorCode.INVALID_STOCK_ADJUSTMENT,
                    String.format("Resulting on-hand quantity (%d) cannot be lower than active reserved quantity (%d)",
                            newOnHand, inventory.getReserved()));
        }
    }

    public void validateCycleCount(Inventory inventory, int physicalCount) {
        if (physicalCount < 0) {
            throw new ApplicationException(ErrorCode.INVALID_STOCK_ADJUSTMENT, "Physical count cannot be negative");
        }
        if (physicalCount < inventory.getReserved()) {
            throw new ApplicationException(ErrorCode.INVALID_STOCK_ADJUSTMENT,
                    String.format("Physical count (%d) cannot be lower than active reserved quantity (%d)",
                            physicalCount, inventory.getReserved()));
        }
    }
}
