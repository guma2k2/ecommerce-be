package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.request.AssignPermissionRequest;
import com.yas.system.auth.internal.dto.request.RoleRequest;
import com.yas.system.auth.internal.dto.response.RoleResponse;
import com.yas.system.auth.internal.service.RoleService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/role")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    RoleService roleService;

    @GetMapping("/page")
    public ApiResponse<PageResponse<RoleResponse>> getRolePage(
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(roleService.getRolePage(pageNumber, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<RoleResponse> getRoleDetail(@PathVariable("id") Integer id) {
        return ApiResponse.success(roleService.getRoleDetail(id));
    }

    @PostMapping
    public ApiResponse<RoleResponse> createRole(@Valid @RequestBody RoleRequest roleRequest) {
        return ApiResponse.success(roleService.createRole(roleRequest));
    }

    @PutMapping("/{id}")
    public ApiResponse<RoleResponse> updateRole(
            @PathVariable("id") Integer id,
            @Valid @RequestBody RoleRequest roleRequest
    ) {
        return ApiResponse.success(roleService.updateRole(id, roleRequest));
    }

    @PatchMapping("/{id}/permissions")
    public ApiResponse<Void> assignPermissions(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AssignPermissionRequest request
    ) {
        roleService.assignPermissions(id, request);
        return ApiResponse.successWithNoContent();
    }
}
