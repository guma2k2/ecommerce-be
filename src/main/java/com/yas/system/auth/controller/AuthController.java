package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.request.*;
import com.yas.system.auth.internal.dto.response.AuthenticationResponse;
import com.yas.system.auth.internal.service.AuthService;
import com.yas.system.auth.internal.util.Constant;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.common.security.annotation.ActiveUser;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public ApiResponse<AuthenticationResponse> signIn(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletResponse response
    ) {
        AuthenticationResponse authenticationResponse = authService.signIn(signInRequest, response);
        return ApiResponse.success(authenticationResponse);
    }

    @PostMapping("/sign-up")
    public ApiResponse<String> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.signUp(signUpRequest);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/send-verification")
    public ApiResponse<Void> sendVerification(@Valid @RequestBody SendVerificationRequest sendVerificationRequest) {
        authService.sendVerificationCode(sendVerificationRequest);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/2fa/send-verification")
    public ApiResponse<Void> send2faVerification(@Valid @RequestBody SendVerificationRequest sendVerificationRequest) {
        authService.sendVerificationCode(sendVerificationRequest);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/2fa/setup")
    public ApiResponse<String> setUp2fa(@ActiveUser AuthUser authUser) {
        String url = authService.setUp2fa(authUser);
        return ApiResponse.success(url);
    }

    @PostMapping("/2fa/enable")
    public ApiResponse<Void> enable2fa(
            @ActiveUser AuthUser authUser,
            @Valid @RequestBody EnableMfaRequest enableMfaRequest
    ) {
        authService.enable2fa(authUser, enableMfaRequest);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/2fa/disable")
    public ApiResponse<Void> disable2fa(@ActiveUser AuthUser authUser) {
        authService.disable2fa(authUser);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/2fa/verify")
    public ApiResponse<Void> verify2fa(
            @Valid @RequestBody VerifyRequest verifyRequest,
            @ActiveUser AuthUser authUser
    ) {
        authService.verifyMfaCode(authUser, verifyRequest);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/verify")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody VerifyRequest verifyRequest) {
        authService.verifyEmail(verifyRequest);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/sign-out")
    public ApiResponse<Void> signOut(@CookieValue(name = Constant.REFRESH_COOKIE_HEADER) String cookieToken) {
        authService.signOut(cookieToken);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/refresh")
    public ApiResponse<String> refreshToken(
            @ActiveUser AuthUser authUser,
            @Parameter(hidden = true) @CookieValue(name = Constant.REFRESH_COOKIE_HEADER) String cookieToken
    ) {
        String accessToken = authService.refreshToken(cookieToken, authUser);
        return ApiResponse.success(accessToken);
    }

    @PostMapping("/outbound")
    public ApiResponse<AuthenticationResponse> outboundAuthentication(
            @CookieValue(value = "oauth2_state", required = false) String savedState,
            @RequestBody OutboundAuthenticationRequest outboundAuthenticationRequest,
            HttpServletResponse response
    ) {
        AuthenticationResponse signInResponse = authService
                .outboundAuthenticate(outboundAuthenticationRequest, savedState, response);
        return ApiResponse.success(signInResponse);
    }

    @GetMapping("/login-in-social/{registrationId}")
    public ApiResponse<String> loginGoogle(
            @PathVariable("registrationId") String registrationId,
            HttpServletResponse response
    ) {
        String url = authService.startOauth2Login(registrationId, response);
        return ApiResponse.success(url);
    }

    // forgot password
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword() {
        return ApiResponse.successWithNoContent();
    }

}
