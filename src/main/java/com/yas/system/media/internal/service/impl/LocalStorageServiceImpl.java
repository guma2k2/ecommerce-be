package com.yas.system.media.internal.service.impl;

import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.media.internal.dto.response.MediaResponse;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.enums.MediaType;
import com.yas.system.media.internal.helper.MediaHelper;
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
    private final MediaHelper mediaHelper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public MediaResponse upload(MultipartFile multipartFile, String altText) {
        MediaType mediaType = mediaHelper.detectMediaType(multipartFile);
        String fileType = mediaHelper.extractFileType(multipartFile);

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

            Media media = mediaHelper.createMedia(
                    finalName,
                    url,
                    mediaType,
                    multipartFile.getSize(),
                    finalAltText,
                    fileType,
                    null
            );

            media = mediaRepository.save(media);
            return MediaResponse.fromModel(media);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file locally", e);
        }
    }
}
