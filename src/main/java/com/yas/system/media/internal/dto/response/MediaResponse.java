package com.yas.system.media.internal.dto.response;

import com.yas.system.media.internal.entity.Media;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
        String url = media.getUrl();
        if (url != null && url.startsWith("/")) {
            try {
                url = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + url;
            } catch (IllegalStateException e) {
                // Keep relative url if called outside of request context (e.g. unit tests)
            }
        }
        return new MediaResponse(
                media.getId().toString(),
                media.getName(),
                url,
                media.getType().toString(),
                media.getSize(),
                media.getAltText(),
                media.getFileType(),
                media.isActive(),
                media.getDuration()
        );
    }
}