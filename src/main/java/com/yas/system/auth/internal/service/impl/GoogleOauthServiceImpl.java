package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.response.GoogleTokenResponse;
import com.yas.system.auth.internal.dto.response.GoogleUserInfoResponse;
import com.yas.system.auth.internal.service.GoogleOauthService;
import com.yas.system.common.config.AppProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GoogleOauthServiceImpl implements GoogleOauthService {

    RestClient restClient;
    AppProperties appProperties;

    @Override
    public GoogleTokenResponse exchangeCodeForToken(String code) {
        var google = appProperties.oauth2().google();

        var form = new LinkedMultiValueMap<String, String>();
        form.add("code", code);
        form.add("client_id", google.clientId());
        form.add("client_secret", google.clientSecret());
        form.add("redirect_uri", google.redirectUri());
        form.add("grant_type", "authorization_code");

        return restClient.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(GoogleTokenResponse.class);
    }

    @Override
    public GoogleUserInfoResponse getUserInfo(String accessToken) {
        return restClient.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .headers(headers -> headers.setBearerAuth(accessToken))
                .retrieve()
                .body(GoogleUserInfoResponse.class);
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        var google = appProperties.oauth2().google();
        System.out.println("google: " + google.clientId());

        return UriComponentsBuilder
                .fromUriString("https://accounts.google.com/o/oauth2/v2/auth")
                .queryParam("client_id", google.clientId())
                .queryParam("redirect_uri", google.redirectUri())
                .queryParam("response_type", "code")
                .queryParam("scope", String.join(" ", google.scope()))
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build()
                .toUriString();
    }


}
