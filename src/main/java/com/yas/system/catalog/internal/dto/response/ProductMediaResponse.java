package com.yas.system.catalog.internal.dto.response;

import com.yas.system.catalog.internal.entity.ProductMedia;
import com.yas.system.media.api.dto.MediaPublicDto;

import java.util.List;

public record ProductMediaResponse(
        String mediaId,
        int position,
        String url,
        String type,
        String altText,
        String duration,
        List<Long> variantIds
) {
    public static ProductMediaResponse from(ProductMedia productMedia, MediaPublicDto mediaDto, List<Long> variantIds) {
        return new ProductMediaResponse(
                productMedia.getMediaId(),
                productMedia.getPosition(),
                mediaDto != null ? mediaDto.url() : null,
                mediaDto != null ? mediaDto.type() : null,
                mediaDto != null ? mediaDto.altText() : null,
                mediaDto != null ? mediaDto.duration() : null,
                variantIds != null ? variantIds : List.of()
        );
    }

    public static ProductMediaResponse from(
            ProductMedia productMedia,
            String url,
            String type,
            String altText,
            String duration,
            List<Long> variantIds
    ) {
        return new ProductMediaResponse(
                productMedia.getMediaId(),
                productMedia.getPosition(),
                url,
                type,
                altText,
                duration,
                variantIds != null ? variantIds : List.of()
        );
    }

    public static ProductMediaResponse from(ProductMedia productMedia, String url, List<Long> variantIds) {
        return from(productMedia, url, null, null, null, variantIds);
    }
}


