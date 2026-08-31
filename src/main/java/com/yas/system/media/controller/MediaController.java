package com.yas.system.media.controller;

import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import com.yas.system.media.internal.dto.response.MediaResponse;
import com.yas.system.media.internal.service.MediaService;
import com.yas.system.media.internal.service.UploadService;
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

    UploadService uploadService;
    MediaService mediaService;

    @PostMapping()
    public ApiResponse<MediaResponse> upload (
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam(value = "altText", required = false) String altText
    ) {
        return ApiResponse.success(uploadService.upload(multipartFile, altText));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<MediaResponse>> getPageMedia (
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(mediaService.getPage(pageNumber, pageSize));
    }


    @GetMapping("/{mediaId}")
    public ApiResponse<MediaResponse> getMediaById (
            @PathVariable("mediaId") String mediaId
    ) {
        return ApiResponse.success(mediaService.getById(mediaId));
    }
}
