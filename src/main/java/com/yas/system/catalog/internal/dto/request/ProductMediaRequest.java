package com.yas.system.catalog.internal.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ProductMediaRequest(
        @NotBlank
        String mediaId,

        @NotBlank
        int position
) {
}
