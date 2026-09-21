package com.yas.system.auth.internal.dto.response;

import com.yas.system.auth.internal.entity.CustomerProfile;
import com.yas.system.auth.internal.enumeration.Gender;

import java.util.UUID;

public record CustomerProfileResponse(
        UUID userId,
        String email,
        String name,
        Gender gender,
        String avatar
) {
    public static CustomerProfileResponse fromModel(CustomerProfile customerProfile) {
        return new CustomerProfileResponse(
                customerProfile.getUserId(),
                customerProfile.getUser() != null ? customerProfile.getUser().getEmail() : null,
                customerProfile.getName(),
                customerProfile.getGender(),
                customerProfile.getAvatar()
        );
    }
}
