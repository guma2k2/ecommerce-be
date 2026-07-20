package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.request.*;
import com.yas.system.auth.internal.dto.response.AuthenticationResponse;
import com.yas.system.common.security.annotation.AuthUser;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthenticationResponse signIn(SignInRequest signInRequest, HttpServletResponse response);
    void signUp(SignUpRequest signUpRequest);
    void signOut(String refreshToken);
    void verifyEmail(VerifyRequest  verifyRequest);
    void sendVerificationCode(SendVerificationRequest sendVerificationRequest);
    void verifyMfaCode(AuthUser authUser, VerifyRequest verifyRequest);
    String refreshToken(String refreshToken, AuthUser authUser);
    String startOauth2Login(String registrationId, HttpServletResponse response);
    AuthenticationResponse outboundAuthenticate(OutboundAuthenticationRequest outboundAuthenticationRequest, String savedState, HttpServletResponse response);
    String setUp2fa(AuthUser authUser);
    void enable2fa(AuthUser authUser, EnableMfaRequest enableMfaRequest);
    void disable2fa(AuthUser authUser);
    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    void resetPasswordRequest(ResetPasswordRequest resetPasswordRequest);
}
