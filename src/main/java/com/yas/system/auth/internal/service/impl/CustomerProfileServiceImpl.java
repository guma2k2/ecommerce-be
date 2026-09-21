package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.response.CustomerProfileResponse;
import com.yas.system.auth.internal.entity.CustomerProfile;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.repository.CustomerProfileRepository;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.auth.internal.service.CustomerProfileService;
import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.security.annotation.AuthUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerProfileServiceImpl implements CustomerProfileService {

    UserRepository userRepository;
    CustomerProfileRepository customerProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerProfileResponse getCustomerProfile(AuthUser authUser) {
        User user = userRepository.findByEmail(authUser.email())
                .orElseThrow(() -> new ApplicationException(ErrorCode.USER_NOT_FOUND));

        CustomerProfile customerProfile = customerProfileRepository.findById(user.getId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.CUSTOMER_PROFILE_NOT_FOUND));

        return CustomerProfileResponse.fromModel(customerProfile);
    }
}
