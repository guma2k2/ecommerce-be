package com.yas.system.media.api.dto;

/**
 * Public DTO representing media details exposed across module boundaries.
 *
 * @param id UUID string of the media
 * @param url Absolute or formatted URL to access the media asset
 * @param type Media type enum name (e.g. "IMAGE", "VIDEO")
 * @param altText Descriptive alternative text for accessibility and SEO
 * @param duration Video duration (e.g. "01:30") or null for images
 */
public record MediaPublicDto(
        String id,
        String url,
        String type,
        String altText,
        String duration
) {
}
