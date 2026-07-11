package com.yas.system.media.internal.service.impl;

import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.ResourceNotFoundException;
import com.yas.system.common.response.PageResponse;
import com.yas.system.media.internal.dto.response.MediaResponse;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.repository.MediaRepository;
import com.yas.system.media.internal.service.MediaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaServiceImpl implements MediaService {

    MediaRepository mediaRepository;

    @Override
    public MediaResponse getById(String id) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException(ErrorCode.MEDIA_NOT_FOUND);
        }
        Media media = mediaRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEDIA_NOT_FOUND));
        return MediaResponse.fromModel(media);
    }

    @Override
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
}
