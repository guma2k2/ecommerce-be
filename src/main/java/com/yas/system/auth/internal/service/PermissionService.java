package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.response.PermissionResponse;
import com.yas.system.common.response.PageResponse;

public interface PermissionService {
    PageResponse<PermissionResponse> getPermissionPage(Integer pageNumber, Integer pageSize);
    PermissionResponse getPermissionDetail(Integer id);
}
