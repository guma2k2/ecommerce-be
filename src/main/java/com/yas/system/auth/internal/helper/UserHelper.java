package com.yas.system.auth.internal.helper;

import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.OauthUserInfo;
import com.yas.system.auth.internal.entity.Permission;
import com.yas.system.auth.internal.entity.Role;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.enumeration.OauthProvider;
import com.yas.system.common.security.annotation.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.yas.system.common.constant.AppConstant.DEFAULT_LANGUAGE;

@Component
@RequiredArgsConstructor
public class UserHelper {

    private final PasswordEncoder passwordEncoder;

    public User createUser(SignUpRequest signUpRequest) {
        User newUser = new User();
        newUser.setEmail(signUpRequest.email());
        newUser.setPassword(passwordEncoder.encode(signUpRequest.password()));
        newUser.setLanguage(signUpRequest.language());
        newUser.setVerified(false);
        newUser.setProvider(OauthProvider.LOCAL);
        return newUser;
    }

    public User createUser(OauthUserInfo oauthUserInfo) {
        User newUser = new User();
        newUser.setEmail(oauthUserInfo.email());
        newUser.setPassword(null);
        newUser.setLanguage(DEFAULT_LANGUAGE);
        newUser.setVerified(true);
        newUser.setProvider(oauthUserInfo.provider());
        return newUser;
    }

    public static AuthUser toAuthUser(User user) {
        String roleName = user.getRoles() != null ? user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(",")) : "";

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(role.getName()));
                if (role.getPermissions() != null) {
                    for (Permission perm : role.getPermissions()) {
                        authorities.add(new SimpleGrantedAuthority(perm.getName()));
                    }
                }
            }
        }

        String userId = user.getId() != null ? user.getId().toString() : null;
        return new AuthUser(userId, user.getEmail(), roleName, user.getPassword(), authorities);
    }
}
