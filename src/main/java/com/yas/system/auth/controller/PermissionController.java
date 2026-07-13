package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.response.PermissionResponse;
import com.yas.system.auth.internal.service.PermissionService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/permission")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionController {

    PermissionService permissionService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<PermissionResponse>> getPermissionPage(
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(permissionService.getPermissionPage(pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<PermissionResponse> getPermissionDetail(@PathVariable("id") Integer id) {
        return ApiResponse.success(permissionService.getPermissionDetail(id));
    }
}
