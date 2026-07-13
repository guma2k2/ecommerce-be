package com.yas.system.auth.internal.service.impl;

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

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;

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
        return RoleResponse.fromModel(role);
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
}
