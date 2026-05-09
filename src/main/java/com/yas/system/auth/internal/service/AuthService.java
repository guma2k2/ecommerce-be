package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.AuthResponse;
import com.yas.system.common.security.AuthUser;

public interface AuthService {
    AuthResponse signIn(SignInRequest signInRequest);
    void signUp(SignUpRequest signUpRequest);
    void signOut();
    String refreshToken(String refreshToken, AuthUser authUser);
}
