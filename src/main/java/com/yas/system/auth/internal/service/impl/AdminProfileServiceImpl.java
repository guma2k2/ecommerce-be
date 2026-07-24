package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.request.AdminProfileRequest;
import com.yas.system.auth.internal.dto.response.AdminProfileResponse;
import com.yas.system.auth.internal.entity.AdminProfile;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.helper.AdminProfileHelper;
import com.yas.system.auth.internal.repository.AdminProfileRepository;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.auth.internal.service.AdminProfileService;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.ResourceNotFoundException;
import com.yas.system.common.security.annotation.AuthUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminProfileServiceImpl implements AdminProfileService {

    UserRepository userRepository;
    AdminProfileRepository adminProfileRepository;
    AdminProfileHelper adminProfileHelper;

    @Override
    @Transactional(readOnly = true)
    public AdminProfileResponse getAdminProfile(AuthUser authUser) {
        User user = userRepository.findByEmail(authUser.email())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        AdminProfile adminProfile = adminProfileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ADMIN_PROFILE_NOT_FOUND));

        return AdminProfileResponse.fromModel(adminProfile);
    }

    @Override
    @Transactional
    public AdminProfileResponse updateAdminProfile(AuthUser authUser, AdminProfileRequest request) {
        User user = userRepository.findByEmail(authUser.email())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        AdminProfile adminProfile = adminProfileRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ADMIN_PROFILE_NOT_FOUND));

        adminProfileHelper.updateAdminProfile(adminProfile, request);

        AdminProfile savedAdminProfile = adminProfileRepository.save(adminProfile);
        return AdminProfileResponse.fromModel(savedAdminProfile);
    }
}
