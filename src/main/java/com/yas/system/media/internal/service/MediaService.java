package com.yas.system.media.internal.service;

import com.yas.system.common.response.PageResponse;
import com.yas.system.media.internal.dto.response.MediaResponse;

public interface MediaService {
    MediaResponse getById(String id);
    PageResponse<MediaResponse> getPage(Integer pageNumber, Integer pageSize);
}
