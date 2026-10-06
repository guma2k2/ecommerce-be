package com.yas.system.inventory.internal.dto.request;

import com.yas.system.common.response.ParamError;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockCycleCountRequest(
        @NotNull(message = ParamError.FIELD_NAME)
        Long productVariantId,

        @NotNull(message = ParamError.FIELD_NAME)
        @Min(value = 0, message = ParamError.MIN)
        Integer physicalCount,

        @Size(max = 100, message = ParamError.MAX_LENGTH)
        String referenceId,

        @Size(max = 500, message = ParamError.MAX_LENGTH)
        String note
) {
}
