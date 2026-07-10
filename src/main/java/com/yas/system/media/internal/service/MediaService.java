package com.yas.system.media.internal.service;

import com.yas.system.media.internal.dto.response.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MediaService {
    MediaResponse upload(MultipartFile multipartFile, String type);
    MediaResponse getById(String id);
}
