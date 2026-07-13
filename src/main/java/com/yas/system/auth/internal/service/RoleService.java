package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.request.RoleRequest;
import com.yas.system.auth.internal.dto.response.RoleResponse;
import com.yas.system.common.response.PageResponse;

public interface RoleService {
    PageResponse<RoleResponse> getRolePage(Integer pageNumber, Integer pageSize);
    RoleResponse getRoleDetail(Integer id);
    RoleResponse createRole(RoleRequest roleRequest);
    RoleResponse updateRole(Integer id, RoleRequest roleRequest);
}
