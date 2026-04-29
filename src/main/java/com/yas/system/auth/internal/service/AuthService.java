package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse signIn(SignInRequest signInRequest);
    void signUp(SignUpRequest signUpRequest);
    void signOut();
}
