package com.yas.system.media.internal.service.impl;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.response.PageResponse;
import com.yas.system.media.api.dto.MediaPublicDto;
import com.yas.system.media.internal.dto.response.MediaResponse;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.helper.MediaHelper;
import com.yas.system.media.internal.repository.MediaRepository;
import com.yas.system.media.service.MediaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaServiceImpl implements MediaService {

    MediaRepository mediaRepository;

    @Override
    @Transactional(readOnly = true)
    public MediaResponse getById(String id) {
        UUID uuid = parseUuid(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEDIA_NOT_FOUND));
        Media media = mediaRepository.findById(uuid)
                .orElseThrow(() -> new ApplicationException(ErrorCode.MEDIA_NOT_FOUND));
        return MediaResponse.fromModel(media);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MediaResponse> getPage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Media> mediaPage = mediaRepository.findAll(pageable);

        List<MediaResponse> content = mediaPage.getContent().stream()
                .map(MediaResponse::fromModel)
                .toList();

        return new PageResponse<>(
                mediaPage.getNumber(),
                mediaPage.getSize(),
                mediaPage.getTotalPages(),
                mediaPage.getTotalElements(),
                content
        );
    }

    @Override
    @Transactional(readOnly = true)
    public String getMediaUrl(String mediaId) {
        return parseUuid(mediaId)
                .flatMap(mediaRepository::findById)
                .map(media -> MediaHelper.formatUrl(media.getUrl()))
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
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
            urlMap.put(media.getId().toString(), MediaHelper.formatUrl(media.getUrl()));
        }
        return urlMap;
    }

    @Override
    @Transactional(readOnly = true)
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
                    MediaHelper.formatUrl(media.getUrl()),
                    media.getType() != null ? media.getType().name() : null,
                    media.getAltText(),
                    media.getDuration()
            ));
        }
        return detailMap;
    }

    private List<UUID> parseUuids(List<String> mediaIds) {
        return mediaIds.stream()
                .map(this::parseUuid)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<UUID> parseUuid(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
