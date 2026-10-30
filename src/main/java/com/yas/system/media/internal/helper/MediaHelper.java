package com.yas.system.media.internal.helper;

import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.enums.MediaType;
import com.yas.system.media.internal.validator.MediaValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaHelper {

    MediaValidator mediaValidator;

    public MediaType detectMediaType(MultipartFile file) {
        return mediaValidator.detectMediaType(file);
    }

    public void validateFileSize(MultipartFile file, MediaType mediaType) {
        mediaValidator.validateFileSize(file, mediaType);
    }

    public MediaType validateMedia(MultipartFile file) {
        return mediaValidator.validateMedia(file);
    }

    public String extractFileType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (!extension.isBlank()) {
            return truncate(extension, 20);
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            if (contentType.contains("/")) {
                String subType = contentType.substring(contentType.lastIndexOf('/') + 1);
                return truncate(subType, 20);
            }
            return truncate(contentType, 20);
        }

        return "unknown";
    }

    public String extractExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT).trim();
        }
        return "";
    }

    public Media createMedia(
            String name,
            String url,
            MediaType type,
            long size,
            String altText,
            String fileType,
            String duration
    ) {
        return Media.builder()
                .name(name)
                .url(url)
                .type(type)
                .size(size)
                .altText(altText)
                .fileType(fileType)
                .duration(duration)
                .active(true)
                .build();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
