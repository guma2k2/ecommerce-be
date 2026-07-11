package com.yas.system.media.internal.dto.response;

import com.yas.system.media.internal.entity.Media;

public record MediaResponse(
        String id,
        String name,
        String url,
        String type,
        long size,
        String altText,
        String fileType,
        boolean active,
        String duration
) {
    public static MediaResponse fromModel(Media media) {
        return new MediaResponse(
                media.getId().toString(),
                media.getName(),
                media.getUrl(),
                media.getType().toString(),
                media.getSize(),
                media.getAltText(),
                media.getFileType(),
                media.isActive(),
                media.getDuration()
        );
    }
}