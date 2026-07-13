package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.response.PermissionResponse;
import com.yas.system.auth.internal.entity.Permission;
import com.yas.system.auth.internal.repository.PermissionRepository;
import com.yas.system.auth.internal.service.PermissionService;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.ResourceNotFoundException;
import com.yas.system.common.response.PageResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {

    PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionResponse> getPermissionPage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Permission> permissionPage = permissionRepository.findAll(pageable);

        List<PermissionResponse> content = permissionPage.getContent().stream()
                .map(PermissionResponse::fromModel)
                .toList();

        return new PageResponse<>(
                permissionPage.getNumber(),
                permissionPage.getSize(),
                permissionPage.getTotalPages(),
                permissionPage.getTotalElements(),
                content
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PermissionResponse getPermissionDetail(Integer id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PERMISSION_NOT_FOUND));
        return PermissionResponse.fromModel(permission);
    }
}
