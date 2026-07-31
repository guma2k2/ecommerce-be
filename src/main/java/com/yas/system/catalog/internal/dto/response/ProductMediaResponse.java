package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.ProductMedia;

import java.util.List;

public record ProductMediaResponse(
        String mediaId,
        int position,
        String url,
        List<Long> variantIds
) {
    public static ProductMediaResponse from(ProductMedia productMedia, String url) {
        return new ProductMediaResponse(
                productMedia.getMediaId(),
                productMedia.getPosition(),
                url,
                List.of()
        );
    }

    public static ProductMediaResponse from(ProductMedia productMedia) {
        return from(productMedia, null);
    }
}

