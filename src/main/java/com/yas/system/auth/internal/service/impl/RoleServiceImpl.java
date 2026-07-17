package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.request.AssignPermissionRequest;
import com.yas.system.auth.internal.dto.request.RoleRequest;
import com.yas.system.auth.internal.dto.response.RoleResponse;
import com.yas.system.auth.internal.entity.Role;
import com.yas.system.auth.internal.repository.RoleRepository;
import com.yas.system.auth.internal.service.RoleService;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
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

import java.util.HashSet;
import java.util.List;
import com.yas.system.auth.internal.entity.Permission;
import com.yas.system.auth.internal.repository.PermissionRepository;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> getRolePage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Role> rolePage = roleRepository.findAll(pageable);

        List<RoleResponse> content = rolePage.getContent().stream()
                .map(RoleResponse::fromModel)
                .toList();

        return new PageResponse<>(
                rolePage.getNumber(),
                rolePage.getSize(),
                rolePage.getTotalPages(),
                rolePage.getTotalElements(),
                content
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleDetail(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND));
        List<Integer> permissionIds = role.getPermissions().stream()
                .map(Permission::getId)
                .toList();
        return RoleResponse.fromModelAndPermission(role, permissionIds);
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest roleRequest) {
        if (roleRepository.existsByName(roleRequest.name())) {
            throw new InvalidDataException(ErrorCode.ROLE_ALREADY_EXISTS);
        }
        Role role = new Role();
        role.setName(roleRequest.name());
        role.setAllowGetAll(roleRequest.isAllowListAll());
        Role savedRole = roleRepository.save(role);
        return RoleResponse.fromModel(savedRole);
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Integer id, RoleRequest roleRequest) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND));

        if (!role.getName().equalsIgnoreCase(roleRequest.name()) && roleRepository.existsByName(roleRequest.name())) {
            throw new InvalidDataException(ErrorCode.ROLE_ALREADY_EXISTS);
        }

        role.setName(roleRequest.name());
        role.setAllowGetAll(roleRequest.isAllowListAll());
        Role savedRole = roleRepository.save(role);
        return RoleResponse.fromModel(savedRole);
    }

    @Override
    @Transactional
    public void assignPermissions(Integer roleId, AssignPermissionRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND));

        List<Permission> permissions = permissionRepository.findAllById(request.permissionIds());
        if (permissions.size() != request.permissionIds().size()) {
            throw new ResourceNotFoundException(ErrorCode.PERMISSION_NOT_FOUND);
        }

        role.setPermissions(new HashSet<>(permissions));
        roleRepository.save(role);
    }

    @Override
    @Transactional
    public void deleteRole(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND));

        if ("SUPER_ADMIN".equalsIgnoreCase(role.getName())) {
            throw new InvalidDataException(ErrorCode.ROLE_CANNOT_BE_DELETED);
        }

        roleRepository.deleteUserRolesByRoleId(id);
        roleRepository.delete(role);
    }
}
