package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.request.SendVerificationRequest;
import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.request.VerifyRequest;
import com.yas.system.auth.internal.dto.response.SignInResponse;
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
    public ApiResponse<SignInResponse> signIn(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletResponse response
    ) {
        SignInResponse signInResponse = authService.signIn(signInRequest, response);
        return ApiResponse.success(signInResponse);
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

    // forgot password

    // google login, facebook login, git login using restClient

    // 2fa login

}
