package com.yas.system.auth.internal.dto.response;

import com.yas.system.auth.internal.entity.Role;

import java.util.ArrayList;
import java.util.List;

public record RoleResponse(
        Integer id,
        String name,
        boolean isAllowListAll,
        List<Integer> permissionIds
) {
    public static RoleResponse fromModel(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.isAllowGetAll(),
                new ArrayList<>()
        );
    }
    public static RoleResponse fromModelAndPermission(Role role, List<Integer> permissionIds) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.isAllowGetAll(),
                permissionIds
        );
    }
}
