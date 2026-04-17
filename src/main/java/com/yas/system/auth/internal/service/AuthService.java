package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.AuthDto;

public interface AuthService {
    void signIn(AuthDto.SignInRequest signInRequest);
    void signUp(AuthDto.SignUpRequest signUpRequest);
    void signOut();
}
