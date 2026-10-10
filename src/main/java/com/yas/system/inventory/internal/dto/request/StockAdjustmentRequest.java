package com.yas.system.inventory.internal.dto.request;

import com.yas.system.common.response.ParamError;
import com.yas.system.inventory.internal.enumeration.MovementReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(
        @NotNull(message = ParamError.FIELD_NAME)
        Long productVariantId,

        @NotNull(message = ParamError.FIELD_NAME)
        Integer quantityChange,

        MovementReason reason,

        @Size(max = 100, message = ParamError.MAX_LENGTH)
        String referenceId,

        @Size(max = 500, message = ParamError.MAX_LENGTH)
        String note
) {
}
