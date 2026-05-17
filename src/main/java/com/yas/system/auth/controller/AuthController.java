package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.SignInResponse;
import com.yas.system.auth.internal.service.AuthService;
import com.yas.system.auth.internal.util.Constant;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.security.AuthUser;
import com.yas.system.common.security.annotation.ActiveUser;
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
        return ApiResponse.success("ok");
    }

    // refresh token
    @PostMapping("/refresh")
    public ApiResponse<String> refreshToken(
            @ActiveUser AuthUser authUser,
            @CookieValue(name = Constant.REFRESH_COOKIE_HEADER) String cookieToken
    ) {
        String accessToken = authService.refreshToken(cookieToken, authUser);
        return ApiResponse.success(accessToken);
    }

    // logout
    @PostMapping("/sign-out")
    public ApiResponse<String> signOut(@CookieValue(name = Constant.REFRESH_COOKIE_HEADER) String cookieToken) {
        authService.signOut(cookieToken);
        return ApiResponse.success("ok");
    }

    // forgot password

    // google login

    // github login

    // 2fa login

}
