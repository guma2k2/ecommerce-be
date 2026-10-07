package com.yas.system.inventory.internal.service;

import com.yas.system.common.response.PageResponse;
import com.yas.system.inventory.internal.dto.response.StockMovementResponse;
import com.yas.system.inventory.internal.entity.StockMovement;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import com.yas.system.inventory.internal.enumeration.MovementType;
import org.springframework.data.domain.Pageable;

public interface StockMovementService {

    PageResponse<StockMovementResponse> getMovementsByVariantId(Long variantId, int pageNumber, int pageSize);

    PageResponse<StockMovementResponse> getMovementsByVariantId(Long variantId, Pageable pageable);

    StockMovement recordMovement(
            Long inventoryId,
            Long productVariantId,
            int quantityChange,
            int quantityBefore,
            int quantityAfter,
            MovementType movementType,
            MovementReason reason,
            String referenceId,
            String note,
            String performedBy
    );
}
