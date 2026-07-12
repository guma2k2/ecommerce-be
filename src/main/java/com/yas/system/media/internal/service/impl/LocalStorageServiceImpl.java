package com.yas.system.media.internal.service.impl;

import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.media.internal.dto.response.MediaResponse;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.enums.MediaType;
import com.yas.system.media.internal.repository.MediaRepository;
import com.yas.system.media.internal.service.UploadService;
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
public class LocalStorageServiceImpl implements UploadService {

    private final MediaRepository mediaRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public MediaResponse upload(MultipartFile multipartFile, String type, String altText) {
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

            String finalName = originalFilename;
            if (finalName == null || finalName.isBlank()) {
                finalName = "unknown";
            }
            String finalAltText = (altText != null && !altText.isBlank()) ? altText : finalName;

            String fileType = "";
            if (extension.startsWith(".")) {
                fileType = extension.substring(1);
            } else if (multipartFile.getContentType() != null) {
                fileType = multipartFile.getContentType();
            }
            if (fileType.length() > 20) {
                fileType = fileType.substring(0, 20);
            }

            Media media = Media.builder()
                    .name(finalName)
                    .url(url)
                    .type(mediaType)
                    .size(multipartFile.getSize())
                    .altText(finalAltText)
                    .fileType(fileType)
                    .active(true)
                    .build();

            media = mediaRepository.save(media);
            return MediaResponse.fromModel(media);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file locally", e);
        }
    }
}
