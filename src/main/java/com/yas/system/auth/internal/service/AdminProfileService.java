package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.request.AdminProfileRequest;
import com.yas.system.auth.internal.dto.response.AdminProfileResponse;
import com.yas.system.common.security.annotation.AuthUser;

public interface AdminProfileService {
    AdminProfileResponse getAdminProfile(AuthUser authUser);

    AdminProfileResponse updateAdminProfile(AuthUser authUser, AdminProfileRequest request);
}
