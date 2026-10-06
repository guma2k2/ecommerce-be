package com.yas.system.inventory.internal.dto.response;

import com.yas.system.inventory.internal.entity.StockMovement;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.enumeration.MovementType;

import java.time.ZonedDateTime;

public record StockMovementResponse(
        Long id,
        Long inventoryId,
        Long productVariantId,
        int quantityChange,
        int quantityBefore,
        int quantityAfter,
        MovementType movementType,
        MovementReason reason,
        String referenceId,
        String note,
        String performedBy,
        ZonedDateTime createdAt
) {
    public static StockMovementResponse from(StockMovement movement) {
        if (movement == null) {
            return null;
        }
        return new StockMovementResponse(
                movement.getId(),
                movement.getInventoryId(),
                movement.getProductVariantId(),
                movement.getQuantityChange(),
                movement.getQuantityBefore(),
                movement.getQuantityAfter(),
                movement.getMovementType(),
                movement.getReason(),
                movement.getReferenceId(),
                movement.getNote(),
                movement.getPerformedBy(),
                movement.getCreatedAt()
        );
    }
}
