package com.yas.system.media.internal.dto.response;

import com.yas.system.common.util.DateTimeUtils;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.helper.MediaHelper;

public record MediaResponse(
        String id,
        String name,
        String url,
        String type,
        long size,
        String altText,
        String fileType,
        boolean active,
        String duration,
        String createdAt,
        String updatedAt
) {
    public static MediaResponse fromModel(Media media) {
        String url = MediaHelper.formatUrl(media.getUrl());
        return new MediaResponse(
                media.getId().toString(),
                media.getName(),
                url,
                media.getType() != null ? media.getType().name() : null,
                media.getSize(),
                media.getAltText(),
                media.getFileType(),
                media.isActive(),
                media.getDuration(),
                DateTimeUtils.format(media.getCreatedAt()),
                DateTimeUtils.format(media.getUpdatedAt())
        );
    }
}