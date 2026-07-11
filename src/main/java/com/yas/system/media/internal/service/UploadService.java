package com.yas.system.media.internal.service;

import com.yas.system.common.response.PageResponse;
import com.yas.system.media.internal.dto.response.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    MediaResponse upload(MultipartFile multipartFile, String type, String name, String altText);
}
