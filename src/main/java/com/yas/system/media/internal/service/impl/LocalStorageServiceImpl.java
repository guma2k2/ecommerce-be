package com.yas.system.media.internal.service.impl;

import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.common.exception.ResourceNotFoundException;
import com.yas.system.media.internal.dto.response.MediaResponse;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.enums.MediaType;
import com.yas.system.media.internal.repository.MediaRepository;
import com.yas.system.media.internal.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@ConditionalOnProperty(name = "app.upload.location", havingValue = "local", matchIfMissing = true)
@Service
@RequiredArgsConstructor
public class LocalStorageServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public MediaResponse upload(MultipartFile multipartFile, String type) {
        MediaType mediaType;
        if ("image".equalsIgnoreCase(type)) {
            mediaType = MediaType.IMAGE;
        } else if ("video".equalsIgnoreCase(type)) {
            mediaType = MediaType.VIDEO;
        } else {
            throw new InvalidDataException(ErrorCode.INVALID_MEDIA_TYPE, type);
        }

        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = multipartFile.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID() + extension;
            Path filePath = Paths.get(uploadDir, uniqueFilename);
            Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String url = "/uploads/" + uniqueFilename;

            Media media = Media.builder()
                    .url(url)
                    .type(mediaType)
                    .active(true)
                    .build();

            media = mediaRepository.save(media);
            return MediaResponse.fromModel(media);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file locally", e);
        }
    }

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
}
