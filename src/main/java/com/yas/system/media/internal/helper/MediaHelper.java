package com.yas.system.media.internal.helper;

import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.enums.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
public class MediaHelper {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "tiff", "ico", "avif", "heic", "heif"
    );

    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "mov", "avi", "mkv", "flv", "wmv", "webm", "m4v", "3gp", "ts", "mpg", "mpeg"
    );

    public MediaType detectMediaType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidDataException(ErrorCode.INVALID_MEDIA_TYPE, "Empty file");
        }

        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String lowerContentType = contentType.toLowerCase(Locale.ROOT);
            if (lowerContentType.startsWith("image/")) {
                return MediaType.IMAGE;
            }
            if (lowerContentType.startsWith("video/")) {
                return MediaType.VIDEO;
            }
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!extension.isBlank()) {
            if (IMAGE_EXTENSIONS.contains(extension)) {
                return MediaType.IMAGE;
            }
            if (VIDEO_EXTENSIONS.contains(extension)) {
                return MediaType.VIDEO;
            }
        }

        String invalidTypeDesc = (contentType != null && !contentType.isBlank()) ? contentType : (!extension.isBlank() ? extension : "unknown");
        throw new InvalidDataException(ErrorCode.INVALID_MEDIA_TYPE, invalidTypeDesc);
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
