package com.yas.system.media.api;

import java.util.List;
import java.util.Map;

/**
 * Public service interface exposed by the Media module for cross-module consumption,
 * adhering to Modular Monolith architecture rules.
 */
public interface MediaPublicService {

    /**
     * Get formatted media URL by mediaId.
     *
     * @param mediaId UUID string of the media
     * @return Formatted absolute/relative media URL, or null if mediaId is invalid or not found
     */
    String getMediaUrl(String mediaId);

    /**
     * Bulk get formatted media URLs by a list of mediaIds.
     *
     * @param mediaIds List of UUID strings
     * @return Map of mediaId -> mediaUrl
     */
    Map<String, String> getMediaUrls(List<String> mediaIds);
}
