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
    public ApiResponse<String> sendVerification(@Valid @RequestBody SendVerificationRequest sendVerificationRequest) {
        authService.sendVerificationCode(sendVerificationRequest);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/verify")
    public ApiResponse<String> signUp(@Valid @RequestBody VerifyRequest verifyRequest) {
        authService.verifyEmail(verifyRequest);
        return ApiResponse.successWithNoContent();
    }

    @PostMapping("/sign-out")
    public ApiResponse<String> signOut(@CookieValue(name = Constant.REFRESH_COOKIE_HEADER) String cookieToken) {
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

    @PostMapping("/outbound/authentication")
    public ApiResponse<AuthenticationResponse> outboundAuthentication(
            @CookieValue(value = "oauth2_state", required = false) String savedState,
            @RequestBody OutboundAuthenticationRequest outboundAuthenticationRequest,
            HttpServletResponse response
    ) {
        AuthenticationResponse signInResponse = authService.outboundAuthenticate(outboundAuthenticationRequest, savedState, response);
        return ApiResponse.success(signInResponse);
    }

    @PostMapping("/sign-in-social")
    public void signInSocial(
            @PathVariable("registryId") String registryId,
            HttpServletResponse response
    ) {
        authService.startOauth2Login(registryId, response);
    }

    // forgot password

    // 2fa login

}
