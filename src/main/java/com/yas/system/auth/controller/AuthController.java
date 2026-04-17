package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.AuthDto;
import com.yas.system.auth.internal.service.AuthService;
import com.yas.system.common.response.ApiResponse;
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
    public ApiResponse<String> signIn(@Valid @RequestBody AuthDto.SignInRequest signInRequest) {
        authService.signIn(signInRequest);
        return ApiResponse.success("ok");
    }

    @PostMapping("/sign-up")
    public ApiResponse<String> signUp(@Valid @RequestBody AuthDto.SignUpRequest signUpRequest) {
        authService.signUp(signUpRequest);
        return ApiResponse.success("ok");
    }

}
