package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.response.SignInResponse;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.helper.UserHelper;
import com.yas.system.auth.internal.redis.entity.RefreshToken;
import com.yas.system.auth.internal.redis.entity.VerifyEmail;
import com.yas.system.auth.internal.redis.service.RefreshTokenService;
import com.yas.system.auth.internal.redis.service.VerifyEmailService;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.auth.internal.service.AuthService;
import com.yas.system.auth.internal.util.Constant;
import com.yas.system.auth.internal.util.CookieUtil;
import com.yas.system.common.config.AppConfig;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.common.exception.ResourceNotFoundException;
import com.yas.system.common.mail.dto.SendEmailRequest;
import com.yas.system.common.mail.service.MailService;
import com.yas.system.common.security.AuthUser;
import com.yas.system.common.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
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
    RefreshTokenService refreshTokenService;
    AppConfig appConfig;
    VerifyEmailService verifyEmailService;
    MailService mailService;

    @Override
    public SignInResponse signIn(SignInRequest signInRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(signInRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        boolean isPasswordMatch = passwordEncoder.matches(signInRequest.password(), signInRequest.password());

        if (!isPasswordMatch) throw new ResourceNotFoundException(ErrorCode.INCORRECT_PASSWORD);

        AuthUser userDetails = AuthUser.fromUser(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshTokenRedis = RefreshToken.builder()
                .token(refreshToken)
                .expiresAt(appConfig.refreshTokenExpiration).build();
        refreshTokenService.saveRefreshToken(refreshTokenRedis);

        ResponseCookie refreshTokenCookie = CookieUtil.createRefreshTokenCookie(refreshToken, false);
        response.addHeader(Constant.REFRESH_COOKIE_HEADER, refreshTokenCookie.toString());
        return new SignInResponse(accessToken);
    }

    @Override
    public void signUp(SignUpRequest signUpRequest) {
        userRepository.findByEmail(signUpRequest.email())
                .orElseThrow(() -> new InvalidDataException(ErrorCode.INVALID_EMAIL));
        User user = userHelper.createUser(signUpRequest);
        User savedUser = userRepository.save(user);

        // generate code
        String verifyCode = "abc123";
        VerifyEmail verifyEmail = VerifyEmail.builder()
                .userId(savedUser.getId())
                .timeToLive(Constant.VERIFY_CODE_TTL)
                .verifyCode(verifyCode)
                .build();

        verifyEmailService.saveVerifyEmail(verifyEmail);
        // send email
        SendEmailRequest request = new SendEmailRequest(signUpRequest.email(), "Verify Email", "Your OTP code is: " + verifyCode + ". It is valid for 10 minutes.", true);
        mailService.sendEmail(request);
    }

    @Override
    public void signOut(String  refreshToken) {
        // Delete refresh token from redis and cookie
        refreshTokenService.deleteRefreshTokenByToken(refreshToken);
        CookieUtil.deleteRefreshTokenCookie(false);
    }

    @Override
    public void verifyEmail(String userId, String verifyCode) {
        VerifyEmail verifyEmail = verifyEmailService.getByUserAndVerifyCode(userId, verifyCode).orElseThrow(() -> new InvalidDataException(ErrorCode.INVALID_CODE));
        User user = userRepository.findByEmail(userId).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        user.setVerified(true);
        userRepository.save(user);
    }

    @Override
    public String refreshToken(String refreshToken, AuthUser authUser) {
        // validate refresh token
        refreshTokenService.getRefreshTokenByToken(refreshToken).orElseThrow(() -> new InvalidDataException(ErrorCode.INVALID_TOKEN));
        // generate access token
        String email = authUser.email();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        AuthUser userDetails = AuthUser.fromUser(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        return accessToken;
    }
}
