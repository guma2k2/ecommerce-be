package com.yas.system.auth.internal.helper;

import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.OauthUserInfo;
import com.yas.system.auth.internal.entity.CustomerProfile;
import com.yas.system.auth.internal.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CustomerProfileHelper {

    public CustomerProfile createCustomerProfile(SignUpRequest signUpRequest, User user) {
        CustomerProfile customerProfile = new CustomerProfile();
        customerProfile.setUser(user);
        customerProfile.setName(signUpRequest.name());
        return customerProfile;
    }

    public CustomerProfile createCustomerProfile(OauthUserInfo oauthUserInfo, User user) {
        CustomerProfile customerProfile = new CustomerProfile();
        customerProfile.setUser(user);
        customerProfile.setName(oauthUserInfo.name());
        customerProfile.setAvatar(oauthUserInfo.imageUrl());
        return customerProfile;
    }
}
