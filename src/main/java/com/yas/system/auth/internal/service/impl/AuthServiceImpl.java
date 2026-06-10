package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.request.SendVerificationRequest;
import com.yas.system.auth.internal.dto.request.SignInRequest;
import com.yas.system.auth.internal.dto.request.SignUpRequest;
import com.yas.system.auth.internal.dto.request.VerifyRequest;
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
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.common.security.jwt.JwtService;
import com.yas.system.common.util.RandomUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    UserRepository userRepository;
    JwtService jwtService;
    UserHelper  userHelper;
    RefreshTokenService refreshTokenService;
    AppConfig appConfig;
    VerifyEmailService verifyEmailService;
    MailService mailService;
    AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public SignInResponse signIn(SignInRequest signInRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.email(),
                        signInRequest.password()
        ));
        AuthUser userDetails = (AuthUser) authentication.getPrincipal();
        log.info("userDetails={}", userDetails);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshTokenRedis = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .token(refreshToken)
                .expiresAt(appConfig.refreshTokenExpiration)
                .build();
        refreshTokenService.saveRefreshToken(refreshTokenRedis);

        ResponseCookie refreshTokenCookie = CookieUtil.createRefreshTokenCookie(refreshToken, false);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
        return new SignInResponse(accessToken);
    }

    @Override
    @Transactional
    public void signUp(SignUpRequest signUpRequest) {
        Optional<User> userOptional = userRepository.findByEmail(signUpRequest.email());
        if(userOptional.isPresent()) {
            throw new InvalidDataException(ErrorCode.INVALID_EMAIL);
        }
        User user = userHelper.createUser(signUpRequest);
        User savedUser = userRepository.save(user);

        // generate code
        String verifyCode = RandomUtil.generatesOtp();
        VerifyEmail verifyEmail = VerifyEmail.builder()
                .userId(savedUser.getId().toString())
                .timeToLive(Constant.VERIFY_CODE_TTL)
                .verifyCode(verifyCode)
                .build();

        verifyEmailService.saveVerifyEmail(verifyEmail);
        // send email
        SendEmailRequest request = new SendEmailRequest(signUpRequest.email(),
                "Verify Email",
                "Your OTP code is: " + verifyCode + ". It is valid for 10 minutes.",
                true
        );
        mailService.sendEmail(request);
    }

    @Override
    public void signOut(String  refreshToken) {
        // Delete refresh token from redis and cookie
        refreshTokenService.deleteRefreshTokenByToken(refreshToken);
        CookieUtil.deleteRefreshTokenCookie(false);
    }

    @Override
    public void verifyEmail(VerifyRequest verifyRequest) {
        VerifyEmail verifyEmail = verifyEmailService.getByVerifyCode(verifyRequest.code())
                .orElseThrow(() -> new InvalidDataException(ErrorCode.INVALID_CODE));

        User user = userRepository.findById(UUID.fromString(verifyEmail.getUserId()))
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        user.setVerified(true);
        userRepository.save(user);
    }

    @Override
    public void sendVerificationCode(SendVerificationRequest sendVerificationRequest) {
        // send email
        String verifyCode = RandomUtil.generatesOtp();
        SendEmailRequest request = new SendEmailRequest(sendVerificationRequest.email(),
                "Verify Email",
                "Your OTP code is: " + verifyCode + ". It is valid for 15 minutes.",
                true
        );
        mailService.sendEmail(request);
    }

    @Override
    public String refreshToken(String refreshToken, AuthUser authUser) {
        // validate refresh token
        refreshTokenService.getRefreshTokenByToken(refreshToken)
                .orElseThrow(() -> new InvalidDataException(ErrorCode.INVALID_TOKEN));
        // generate access token
        String email = authUser.email();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        AuthUser userDetails = AuthUser.fromUser(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        return accessToken;
    }

    @Override
    public void outboundAuthenticate(String code) {

    }
}
