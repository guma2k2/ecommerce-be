package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.request.*;
import com.yas.system.auth.internal.dto.response.*;
import com.yas.system.auth.internal.entity.User;
import com.yas.system.auth.internal.enums.OauthProvider;
import com.yas.system.auth.internal.helper.UserHelper;
import com.yas.system.auth.internal.mfa.MfaService;
import com.yas.system.auth.internal.redis.entity.RefreshToken;
import com.yas.system.auth.internal.redis.entity.VerifyEmail;
import com.yas.system.auth.internal.redis.service.RefreshTokenService;
import com.yas.system.auth.internal.redis.service.VerifyEmailService;
import com.yas.system.auth.internal.repository.UserRepository;
import com.yas.system.auth.internal.service.AuthService;
import com.yas.system.auth.internal.service.FacebookOauthService;
import com.yas.system.auth.internal.service.GithubOauthService;
import com.yas.system.auth.internal.service.GoogleOauthService;
import com.yas.system.auth.internal.util.Constant;
import com.yas.system.auth.internal.util.CookieUtil;
import com.yas.system.common.config.AppProperties;
import com.yas.system.common.exception.ErrorCode;
import com.yas.system.common.exception.InvalidDataException;
import com.yas.system.common.exception.ResourceNotFoundException;
import com.yas.system.notification.events.VerifyEmailEvent;
import com.yas.system.common.security.annotation.AuthUser;
import com.yas.system.common.security.jwt.JwtService;
import com.yas.system.common.util.RandomUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
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
    AppProperties appProperties;
    VerifyEmailService verifyEmailService;
    AuthenticationManager authenticationManager;
    GoogleOauthService googleOauthService;
    GithubOauthService githubOauthService;
    FacebookOauthService facebookOauthService;
    ApplicationEventPublisher eventPublisher;
    MfaService mfaService;
    String ISSUER = "me";

    @Override
    @Transactional
    public AuthenticationResponse signIn(SignInRequest signInRequest, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.email(),
                        signInRequest.password()
        ));
        AuthUser userDetails = (AuthUser) authentication.getPrincipal();
        log.info("userDetails={}", userDetails);
        String accessToken = jwtService.generateAccessToken(userDetails);
        responseRefreshToken(response, userDetails);
        return new AuthenticationResponse(accessToken);
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
        VerifyEmailEvent verifyEmailEvent = new VerifyEmailEvent(signUpRequest.email(),
                signUpRequest.name(),
                verifyCode,
                Constant.VERIFY_CODE_TTL_MINUTES
        );
        eventPublisher.publishEvent(verifyEmailEvent);
    }

    @Override
    public void signOut(String  refreshToken) {
        // Delete refresh token from redis and cookie
        refreshTokenService.deleteRefreshTokenByToken(refreshToken);
        CookieUtil.deleteCookie(Constant.REFRESH_COOKIE_HEADER,false);
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
        User user = userRepository.findByEmail(sendVerificationRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        // send email
        String verifyCode = RandomUtil.generatesOtp();
        VerifyEmail verifyEmail = VerifyEmail.builder()
                .userId(user.getId().toString())
                .timeToLive(Constant.VERIFY_CODE_TTL)
                .verifyCode(verifyCode)
                .build();

        verifyEmailService.saveVerifyEmail(verifyEmail);
        VerifyEmailEvent verifyEmailEvent = new VerifyEmailEvent(sendVerificationRequest.email(),
                user.getName(),
                verifyCode,
                Constant.VERIFY_CODE_TTL_MINUTES
        );
        eventPublisher.publishEvent(verifyEmailEvent);
    }



    @Override
    public String refreshToken(String refreshToken, AuthUser authUser) {
        // validate refresh token
        refreshTokenService.getRefreshTokenByToken(refreshToken)
                .orElseThrow(() -> new InvalidDataException(ErrorCode.INVALID_TOKEN));
        // generate access token
        String email = authUser.email();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        AuthUser userDetails = AuthUser.fromUser(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        return accessToken;
    }


    @Override
    public String startOauth2Login(@NotNull String registrationId, HttpServletResponse response) {
        OauthProvider provider = OauthProvider.valueOf(registrationId.toUpperCase());
        var state = generateState();
        var url = switch (provider){
            case GOOGLE -> googleOauthService.buildAuthorizationUrl(state);
            case GITHUB -> githubOauthService.buildAuthorizationUrl(state);
            case FACEBOOK ->  facebookOauthService.buildAuthorizationUrl(state);
            default -> null;
        };
        var stateCookie = CookieUtil.createCookie(Constant.OAUTH2_STATE, state, false);
        response.addHeader(HttpHeaders.SET_COOKIE, stateCookie.toString());
        return url;
    }

    @Override
    public AuthenticationResponse outboundAuthenticate(
            OutboundAuthenticationRequest outboundAuthenticationRequest,
            String savedState,
            HttpServletResponse response
    ) {
        String code = outboundAuthenticationRequest.code();
        String state = outboundAuthenticationRequest.state();
        log.info("outboundAuthenticationRequest={}", outboundAuthenticationRequest);
        log.info("savedState={}", savedState);
        OauthProvider provider = OauthProvider.valueOf(outboundAuthenticationRequest.registrationId().toUpperCase());

        if (code == null || state == null || !state.equals(savedState)) {
            throw new InvalidDataException(ErrorCode.UNCATEGORIZED);
        }

        OauthUserInfo oauthUserInfo = switch (provider) {
            case GOOGLE:
                GoogleTokenResponse googleTokenResponse = googleOauthService.exchangeCodeForToken(code);
                GoogleUserInfoResponse googleUserInfoResponse = googleOauthService
                        .getUserInfo(googleTokenResponse.accessToken());
                yield OauthUserInfo.fromGoogleOauthUser(googleUserInfoResponse);
            case GITHUB:
                GithubTokenResponse githubTokenResponse = githubOauthService.exchangeCodeForToken(code);
                GithubUserInfoResponse githubUserInfoResponse = githubOauthService
                        .getUserInfo(githubTokenResponse.accessToken());
                var emails =
                        githubOauthService.getEmails(githubTokenResponse.accessToken());
                String email = emails.stream()
                        .filter(e -> e.primary() && e.verified())
                        .map(GithubEmailResponse::email)
                        .findFirst()
                        .orElse(null);
                yield OauthUserInfo.fromGithubOauthUser(githubUserInfoResponse, email);

            case FACEBOOK:
                FacebookTokenResponse facebookTokenResponse = facebookOauthService.exchangeCodeForToken(code);
                FacebookUserInfoResponse facebookUserInfoResponse = facebookOauthService
                        .getUserInfo(facebookTokenResponse.accessToken());
                yield OauthUserInfo.fromFacebookOauthUser(facebookUserInfoResponse);
            default:
                yield null;
        };

        if (Objects.isNull(oauthUserInfo)) {
            throw new InvalidDataException(ErrorCode.UNCATEGORIZED);
        }


        User activeUser = userRepository.findByEmail(oauthUserInfo.email()).orElseGet(() -> {
            User user = userHelper.createUser(oauthUserInfo);
            return userRepository.save(user);
        });

        if (!activeUser.getProvider().equals(provider)) {
            throw new InvalidDataException(ErrorCode.INVALID_PROVIDER, provider, provider);
        }

        AuthUser userDetails = AuthUser.fromUser(activeUser);
        String accessToken = jwtService.generateAccessToken(userDetails);
        responseRefreshToken(response, userDetails);

        CookieUtil.deleteCookie(Constant.OAUTH2_STATE,false);
        return new AuthenticationResponse(accessToken);
    }

    @Override
    public String setUp2fa(AuthUser authUser) {
        User user = userRepository.findByEmail(authUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));

        String mfaSecret = mfaService.generateMfaSecret(authUser.email());
        String url = mfaService.generateQrCodeUri(authUser.email(), mfaSecret, ISSUER);
        user.setMfaSecret(mfaSecret);
        userRepository.save(user);
        return url;
    }

    @Override
    public void verifyMfaCode(AuthUser authUser, VerifyRequest verifyRequest) {
        User user = userRepository.findByEmail(authUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        if (!mfaService.verifyTotpCode(user.getMfaSecret(),  verifyRequest.code())) {
            throw new InvalidDataException(ErrorCode.INVALID_CODE);
        }
    }

    @Override
    public void enable2fa(AuthUser authUser, EnableMfaRequest enableMfaRequest) {
        User user = userRepository.findByEmail(authUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        if (!mfaService.verifyTotpCode(user.getMfaSecret(),  enableMfaRequest.code())) {
            throw new InvalidDataException(ErrorCode.INVALID_CODE);
        }
        user.setEnabledMfa(true);
        userRepository.save(user);
    }

    @Override
    public void disable2fa(AuthUser authUser) {
        User user = userRepository.findByEmail(authUser.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND));
        user.setEnabledMfa(false);
        user.setMfaSecret(null);
        userRepository.save(user);
    }

    private void responseRefreshToken(HttpServletResponse response, AuthUser userDetails) {
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshTokenRedis = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .token(refreshToken)
                .expiresAt(appProperties.jwt().refreshTokenExpirationMs())
                .build();
        refreshTokenService.saveRefreshToken(refreshTokenRedis);

        ResponseCookie refreshTokenCookie = CookieUtil
                .createCookie(Constant.REFRESH_COOKIE_HEADER, refreshToken, false);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
