package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.AuthResponse;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.helper.UserHelper;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.auth.internal.service.AuthService;
import com.yas.system.common.security.AuthUser;
import com.yas.system.common.security.JwtService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    JwtService jwtService;
    UserHelper  userHelper;

    @Override
    public AuthResponse signIn(SignInRequest signInRequest) {
        User user = userRepository.findByEmail(signInRequest.email()).orElseThrow(() -> new RuntimeException(""));
        boolean isPasswordMatch = passwordEncoder.matches(signInRequest.password(), signInRequest.password());
        if (isPasswordMatch) throw new RuntimeException("");
        AuthUser userDetails = AuthUser.fromUser(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        log.info("accessToken:{} refreshToken:{}", accessToken, refreshToken);
        return new AuthResponse(accessToken, refreshToken);
    }

    @Override
    public void signUp(SignUpRequest signUpRequest) {
        boolean isEmailExisted = userRepository.findByEmail(signUpRequest.email()).isPresent();
        if (isEmailExisted) throw new RuntimeException("");
        User user = userHelper.createUser(signUpRequest);
        userRepository.save(user);
        //  Todo: Send email verification
    }

    @Override
    public void signOut() {}
}
