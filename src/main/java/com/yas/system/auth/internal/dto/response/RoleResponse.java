package com.yas.system.auth.internal.dto.response;

import com.yas.system.auth.internal.entity.Role;

public record RoleResponse(
        Integer id,
        String name,
        boolean isAllowListAll
) {
    public static RoleResponse fromModel(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.isAllowGetAll()
        );
    }
}
