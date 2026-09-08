package com.yas.system.media.internal.helper;

import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.media.internal.entity.Media;
import com.yas.system.media.internal.enums.MediaType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.Set;

@Component
public class MediaHelper {

    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp", "svg", "tiff", "ico", "avif", "heic", "heif"
    );

    private static final Set<String> VIDEO_EXTENSIONS = Set.of("mp4");

    @Value("${app.upload.max-image-size:10MB}")
    private DataSize maxImageSize;

    @Value("${app.upload.max-video-size:50MB}")
    private DataSize maxVideoSize;

    public MediaType detectMediaType(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApplicationException(ErrorCode.INVALID_MEDIA_TYPE, "Empty file");
        }

        String contentType = file.getContentType();
        String lowerContentType = (contentType != null && !contentType.isBlank())
                ? contentType.toLowerCase(Locale.ROOT).trim()
                : "";
        String extension = extractExtension(file.getOriginalFilename());

        // Accept standard image types
        if (lowerContentType.startsWith("image/") || IMAGE_EXTENSIONS.contains(extension)) {
            return MediaType.IMAGE;
        }

        // Accept MP4 video type only
        if (isMp4Video(lowerContentType, extension)) {
            return MediaType.VIDEO;
        }

        String invalidTypeDesc = !lowerContentType.isBlank() ? lowerContentType : (!extension.isBlank() ? extension : "unknown");
        throw new ApplicationException(ErrorCode.INVALID_MEDIA_TYPE, invalidTypeDesc);
    }

    public void validateFileSize(MultipartFile file, MediaType mediaType) {
        if (file == null || file.isEmpty()) {
            throw new ApplicationException(ErrorCode.INVALID_MEDIA_TYPE, "Empty file");
        }

        long size = file.getSize();
        if (mediaType == MediaType.IMAGE && size > maxImageSize.toBytes()) {
            throw new ApplicationException(ErrorCode.FILE_TOO_LARGE, formatSize(maxImageSize.toBytes()));
        } else if (mediaType == MediaType.VIDEO && size > maxVideoSize.toBytes()) {
            throw new ApplicationException(ErrorCode.FILE_TOO_LARGE, formatSize(maxVideoSize.toBytes()));
        }
    }

    public MediaType validateMedia(MultipartFile file) {
        MediaType mediaType = detectMediaType(file);
        validateFileSize(file, mediaType);
        return mediaType;
    }

    private boolean isMp4Video(String lowerContentType, String extension) {
        if ("video/mp4".equals(lowerContentType)) {
            return true;
        }
        if ("mp4".equalsIgnoreCase(extension)) {
            return lowerContentType.isEmpty()
                    || "application/octet-stream".equals(lowerContentType)
                    || "video/mp4".equals(lowerContentType);
        }
        return false;
    }

    private String formatSize(long sizeInBytes) {
        if (sizeInBytes >= 1024 * 1024) {
            return (sizeInBytes / (1024 * 1024)) + "MB";
        }
        if (sizeInBytes >= 1024) {
            return (sizeInBytes / 1024) + "KB";
        }
        return sizeInBytes + "B";
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
