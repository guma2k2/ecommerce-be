package com.yas.system.auth.internal.dto.response;

import com.yas.system.auth.internal.entity.Permission;

public record PermissionResponse(
        Integer id,
        String name,
        String api,
        String method,
        String module
) {
    public static PermissionResponse fromModel(Permission permission) {
        return new PermissionResponse(
                permission.getId(),
                permission.getName(),
                permission.getApi(),
                permission.getMethod() != null ? permission.getMethod().name() : null,
                permission.getModule() != null ? permission.getModule().name() : null
        );
    }
}
