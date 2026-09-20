package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.response.CustomerProfileResponse;
import com.yas.system.common.security.annotation.AuthUser;

public interface CustomerProfileService {
    CustomerProfileResponse getCustomerProfile(AuthUser authUser);
}

