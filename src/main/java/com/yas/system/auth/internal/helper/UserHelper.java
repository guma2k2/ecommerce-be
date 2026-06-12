package com.yas.system.auth.internal.helper;

import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.OauthUserInfo;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.enums.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHelper {

    private final PasswordEncoder passwordEncoder;

    public User createUser(SignUpRequest signUpRequest) {
        User newUser = new User();
        newUser.setEmail(signUpRequest.email());
        newUser.setPassword(passwordEncoder.encode(signUpRequest.password()));
        newUser.setName(signUpRequest.name());
        newUser.setVerified(false);
        newUser.setProvider(OauthProvider.LOCAL);
        return newUser;
    }

    public User createUser(OauthUserInfo oauthUserInfo) {
        User newUser = new User();
        newUser.setEmail(oauthUserInfo.email());
        newUser.setPassword(null);
        newUser.setName(oauthUserInfo.name());
        newUser.setVerified(true);
        newUser.setProvider(oauthUserInfo.provider());
        return newUser;
    }
}
