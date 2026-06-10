package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.request.SendVerificationRequest;
import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.request.VerifyRequest;
import com.yas.system.auth.internal.dto.response.SignInResponse;
import com.yas.system.common.security.annotation.AuthUser;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    SignInResponse signIn(SignInRequest signInRequest, HttpServletResponse response);
    void signUp(SignUpRequest signUpRequest);
    void signOut(String refreshToken);
    void verifyEmail(VerifyRequest  verifyRequest);
    void sendVerificationCode(SendVerificationRequest sendVerificationRequest);
    String refreshToken(String refreshToken, AuthUser authUser);
    void outboundAuthenticate(String code);
}
