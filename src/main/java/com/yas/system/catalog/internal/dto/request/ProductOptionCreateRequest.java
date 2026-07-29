package com.yas.system.catalog.internal.dto.request;

import com.yas.system.catalog.internal.entity.option.ProductOption;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ProductOptionCreateRequest(
        @NotBlank
        String name
) {
}
