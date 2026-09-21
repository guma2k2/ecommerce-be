package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.response.CustomerProfileResponse;
import com.yas.system.auth.internal.service.CustomerProfileService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.security.annotation.ActiveUser;
import com.yas.system.common.security.annotation.AuthUser;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/customer-profile")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerProfileController {

    CustomerProfileService customerProfileService;

    @GetMapping("/my-profile")
    public ApiResponse<CustomerProfileResponse> getCustomerProfile(@ActiveUser AuthUser authUser) {
        return ApiResponse.success(customerProfileService.getCustomerProfile(authUser));
    }
}
