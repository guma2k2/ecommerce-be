package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.request.AdminProfileRequest;
import com.yas.system.auth.internal.dto.response.AdminProfileResponse;
import com.yas.system.auth.internal.service.AdminProfileService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.security.annotation.ActiveUser;
import com.yas.system.common.security.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/admin-profile")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminProfileController {

    AdminProfileService adminProfileService;

    @GetMapping("/my-profile")
    public ApiResponse<AdminProfileResponse> getAdminProfile(@ActiveUser AuthUser authUser) {
        return ApiResponse.success(adminProfileService.getAdminProfile(authUser));
    }

    @PutMapping("/my-profile")
    public ApiResponse<AdminProfileResponse> updateAdminProfile(
            @ActiveUser AuthUser authUser,
            @Valid @RequestBody AdminProfileRequest request
    ) {
        return ApiResponse.success(adminProfileService.updateAdminProfile(authUser, request));
    }
}
