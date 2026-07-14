package com.yas.system.auth.internal.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AssignPermissionRequest(
        @NotEmpty
        List<Integer> permissionIds
) {
}
