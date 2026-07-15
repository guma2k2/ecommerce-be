package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.request.UserRequest;
import com.yas.system.auth.internal.dto.response.UserResponse;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.entity.Role;
import com.yas.system.auth.internal.enums.OauthProvider;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.auth.internal.repository.RoleRepository;
import com.yas.system.auth.internal.service.UserService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.findByEmail(userRequest.email()).isPresent()) {
            throw new InvalidDataException(ErrorCode.INVALID_EMAIL);
        }

        List<Role> roles = roleRepository.findAllById(userRequest.roleIds());
        if (roles.size() != userRequest.roleIds().size()) {
            throw new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND);
        }

        User user = new User();
        user.setEmail(userRequest.email());
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        user.setName(userRequest.name());
        user.setVerified(true);
        user.setProvider(OauthProvider.LOCAL);
        user.setRoles(new HashSet<>(roles));

        User savedUser = userRepository.save(user);
        return UserResponse.fromModel(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.fromModel(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UserRequest userRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        if (!user.getEmail().equalsIgnoreCase(userRequest.email())) {
            if (userRepository.findByEmail(userRequest.email()).isPresent()) {
                throw new InvalidDataException(ErrorCode.INVALID_EMAIL);
            }
            user.setEmail(userRequest.email());
        }

        if (userRequest.password() != null && !userRequest.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(userRequest.password()));
        }

        List<Role> roles = roleRepository.findAllById(userRequest.roleIds());
        if (roles.size() != userRequest.roleIds().size()) {
            throw new ResourceNotFoundException(ErrorCode.ROLE_NOT_FOUND);
        }

        user.setName(userRequest.name());
        user.setRoles(new HashSet<>(roles));

        User savedUser = userRepository.save(user);
        return UserResponse.fromModel(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUserPage(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponse> content = userPage.getContent().stream()
                .map(UserResponse::fromModel)
                .toList();

        return new PageResponse<>(
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                content
        );
    }
}
