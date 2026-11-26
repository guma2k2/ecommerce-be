package com.yas.system.common.security.annotation;

import com.yas.system.common.exception.ApplicationException;
import com.yas.system.common.exception.ErrorCode;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record AuthUser(
        String id,
        String email,
        String roleName,
        String password,
        Collection<? extends GrantedAuthority> authorities
) implements UserDetails {

    public AuthUser(String email, String roleName, String password, Collection<? extends GrantedAuthority> authorities) {
        this(null, email, roleName, password, authorities);
    }

    public static void requireAuthenticated(AuthUser user) {
        if (user == null || user.id() == null) {
            throw new ApplicationException(ErrorCode.UNAUTHENTICATED);
        }
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
