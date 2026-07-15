package com.yas.system.common.security.annotation;

import com.yas.system.auth.internal.entity.Role;
import com.yas.system.auth.internal.entity.Permission;
import com.yas.system.auth.internal.entity.User;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record AuthUser(
        String email,
        String roleName,
        String password,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    public static AuthUser fromUser(User user) {
        String roleName = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.joining(","));

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

        return new AuthUser(user.getEmail(), roleName, user.getPassword(), authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
