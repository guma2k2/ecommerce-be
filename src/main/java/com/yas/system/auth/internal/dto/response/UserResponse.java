package com.yas.system.auth.internal.dto.response;

import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.entity.Role;
import java.util.List;

public record UserResponse(
        String id,
        String email,
        boolean isEnabledMfa,
        List<Integer> roleIds
) {
    public static UserResponse fromModel(User user) {
        return new UserResponse(
                user.getId().toString(),
                user.getEmail(),
                user.isEnabledMfa(),
                user.getRoles().stream().map(Role::getId).toList()
        );
    }
}
