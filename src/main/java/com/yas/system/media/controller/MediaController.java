package com.yas.system.media.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.media.internal.dto.response.MediaResponse;
import com.yas.system.media.internal.service.MediaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/medias")
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@RequiredArgsConstructor
public class MediaController {

    MediaService mediaService;

    @PostMapping()
    public ApiResponse<MediaResponse> upload (
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("type") String type // video or image
    ) {
        return ApiResponse.success(mediaService.upload(multipartFile, type));
    }

    @GetMapping("/{mediaId}")
    public ApiResponse<MediaResponse> getMediaById (
            @PathVariable("mediaId") String mediaId
    ) {
        return ApiResponse.success(mediaService.getById(mediaId));
    }
}
