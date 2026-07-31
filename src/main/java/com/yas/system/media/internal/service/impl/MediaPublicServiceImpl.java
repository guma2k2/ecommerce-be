package com.yas.system.media.internal.service.impl;

import com.yas.system.media.api.MediaPublicService;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.repository.MediaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaPublicServiceImpl implements MediaPublicService {

    MediaRepository mediaRepository;

    @Override
    public String getMediaUrl(String mediaId) {
        if (mediaId == null || mediaId.isBlank()) {
            return null;
        }
        UUID uuid;
        try {
            uuid = UUID.fromString(mediaId);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return mediaRepository.findById(uuid)
                .map(this::formatUrl)
                .orElse(null);
    }

    @Override
    public Map<String, String> getMediaUrls(List<String> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> uuids = mediaIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(id -> {
                    try {
                        return UUID.fromString(id);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if (uuids.isEmpty()) {
            return Map.of();
        }

        List<Media> medias = mediaRepository.findAllById(uuids);
        Map<String, String> urlMap = new HashMap<>();
        for (Media media : medias) {
            urlMap.put(media.getId().toString(), formatUrl(media));
        }
        return urlMap;
    }

    private String formatUrl(Media media) {
        String url = media.getUrl();
        if (url != null && url.startsWith("/")) {
            try {
                url = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + url;
            } catch (IllegalStateException e) {
                // Fallback for non-request context (e.g. background tasks or unit tests)
            }
        }
        return url;
    }
}
