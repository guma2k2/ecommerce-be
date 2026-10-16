package com.yas.system.media.internal.service.impl;

import com.yas.system.media.api.MediaPublicService;
import com.yas.system.media.api.dto.MediaPublicDto;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.repository.MediaRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

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
        List<UUID> uuids = parseUuids(mediaIds);
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

    @Override
    public Map<String, MediaPublicDto> getMediaDetails(List<String> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> uuids = parseUuids(mediaIds);
        if (uuids.isEmpty()) {
            return Map.of();
        }

        List<Media> medias = mediaRepository.findAllById(uuids);
        Map<String, MediaPublicDto> detailMap = new HashMap<>();
        for (Media media : medias) {
            detailMap.put(media.getId().toString(), new MediaPublicDto(
                    media.getId().toString(),
                    formatUrl(media),
                    media.getType() != null ? media.getType().name() : null,
                    media.getAltText(),
                    media.getDuration()
            ));
        }
        return detailMap;
    }

    private List<UUID> parseUuids(List<String> mediaIds) {
        return mediaIds.stream()
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

