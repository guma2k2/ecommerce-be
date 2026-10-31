package com.yas.system.media.service;

import com.yas.system.common.response.PageResponse;
import com.yas.system.media.api.dto.MediaPublicDto;
import com.yas.system.media.internal.dto.response.MediaResponse;

import java.util.List;
import java.util.Map;

public interface MediaService {
    MediaResponse getById(String id);

    PageResponse<MediaResponse> getPage(Integer pageNumber, Integer pageSize);

    String getMediaUrl(String mediaId);

    Map<String, String> getMediaUrls(List<String> mediaIds);

    Map<String, MediaPublicDto> getMediaDetails(List<String> mediaIds);
}
