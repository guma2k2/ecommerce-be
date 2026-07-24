package com.yas.system.auth.internal.dto.response;

import com.yas.system.auth.internal.entity.AdminProfile;

import java.util.UUID;

public record AdminProfileResponse(
        UUID userId,
        String email,
        String name,
        String avatar
) {
    public static AdminProfileResponse fromModel(AdminProfile adminProfile) {
        return new AdminProfileResponse(
                adminProfile.getUserId(),
                adminProfile.getUser() != null ? adminProfile.getUser().getEmail() : null,
                adminProfile.getName(),
                adminProfile.getAvatar()
        );
    }
}
