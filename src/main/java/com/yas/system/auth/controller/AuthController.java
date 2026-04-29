package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.AuthResponse;
import com.yas.system.auth.internal.service.AuthService;
import com.yas.system.common.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        Cookie cookie = new Cookie("refresh_token", authResponse.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(cookie);
        return ApiResponse.success(authResponse);
    }

    @PostMapping("/sign-up")
    public ApiResponse<String> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        authService.signUp(signUpRequest);
        return ApiResponse.success("ok");
    }

}
