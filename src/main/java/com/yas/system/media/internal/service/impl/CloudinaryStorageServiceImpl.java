package com.yas.system.media.internal.service.impl;

import com.cloudinary.Cloudinary;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.media.internal.dto.response.MediaResponse;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.enums.MediaType;
import com.yas.system.media.internal.repository.MediaRepository;
import com.yas.system.media.internal.service.UploadService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@ConditionalOnProperty(name = "app.upload.location", havingValue = "cloudinary")
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryStorageServiceImpl implements UploadService {

    Cloudinary cloudinary;
    MediaRepository mediaRepository;

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
            Map<?, ?> uploadResult = cloudinary
                    .uploader()
                    .upload(multipartFile.getBytes(), Map.of("resource_type", "auto"));
            String url = (String) uploadResult.get("secure_url");
            if (url == null) {
                url = (String) uploadResult.get("url");
            }
            Object durationObj = uploadResult.get("duration");
            String duration = durationObj != null ? durationObj.toString() : null;

            String originalFilename = multipartFile.getOriginalFilename();
            String finalName = originalFilename;
            if (finalName == null || finalName.isBlank()) {
                finalName = "unknown";
            }
            String finalAltText = (altText != null && !altText.isBlank()) ? altText : finalName;

            String fileType = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileType = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
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
                    .duration(duration)
                    .active(true)
                    .build();

            media = mediaRepository.save(media);
            return MediaResponse.fromModel(media);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }
}
