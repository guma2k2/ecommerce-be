package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.AuthResponse;
import com.yas.system.auth.internal.service.AuthService;
import com.yas.system.auth.internal.util.CookieUtil;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.security.AuthUser;
import com.yas.system.common.security.annotation.ActiveUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public ApiResponse<AuthResponse> signIn(
            @Valid @RequestBody SignInRequest signInRequest,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.signIn(signInRequest);
        ResponseCookie refreshTokenCookie = CookieUtil.createRefreshTokenCookie(authResponse.refreshToken(), false);
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        return ApiResponse.success(authResponse);
    }

    @PostMapping("/sign-up")
    public ApiResponse<String> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.signUp(signUpRequest);
        return ApiResponse.success("ok");
    }

    // refresh token
    @PostMapping("/refresh-token")
    public ApiResponse<String> refreshToken(
            @ActiveUser AuthUser authUser,
            @CookieValue(name = "refresh_token", required = false) String cookieToken
    ) {
        String accessToken = authService.refreshToken(cookieToken, authUser);
        return ApiResponse.success("");
    }

    // logout

    // forgot password

    // google login

    // github login

    // 2fa login

}
