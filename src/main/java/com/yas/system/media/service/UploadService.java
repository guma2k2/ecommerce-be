package com.yas.system.media.service;

import com.yas.system.media.internal.dto.response.MediaResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    MediaResponse upload(MultipartFile multipartFile, String altText);
}

