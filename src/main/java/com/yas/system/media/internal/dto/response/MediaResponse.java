package com.yas.system.media.internal.dto.response;

import com.yas.system.media.internal.entity.Media;

public record MediaResponse(
        String id,
        String url,
        String type,
        boolean active,
        String duration
) {
    public static MediaResponse fromModel(Media media) {
        return new MediaResponse(media.getId().toString(), media.getUrl(), media.getType().toString(), media.isActive() ,media.getDuration());
    }
}