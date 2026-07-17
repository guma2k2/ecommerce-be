package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.request.AssignPermissionRequest;
import com.yas.system.auth.internal.dto.request.RoleRequest;
import com.yas.system.auth.internal.dto.response.RoleResponse;
import com.yas.system.common.response.PageResponse;
import jakarta.validation.Valid;

public interface RoleService {
    PageResponse<RoleResponse> getRolePage(Integer pageNumber, Integer pageSize);
    RoleResponse getRoleDetail(Integer id);
    RoleResponse createRole(RoleRequest roleRequest);
    RoleResponse updateRole(Integer id, RoleRequest roleRequest);
    void assignPermissions(Integer roleId, AssignPermissionRequest request);
    void deleteRole(Integer id);
}
