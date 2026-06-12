package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.response.FacebookTokenResponse;
import com.yas.system.auth.internal.dto.response.FacebookUserInfoResponse;
import com.yas.system.auth.internal.service.FacebookOauthService;
import com.yas.system.common.config.AppProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FacebookOauthServiceImpl implements FacebookOauthService {
    RestClient restClient;
    AppProperties appProperties;

    @Override
    public FacebookTokenResponse exchangeCodeForToken(String code) {
        var facebook = appProperties.oauth2().facebook();

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("graph.facebook.com")
                        .path("/v23.0/oauth/access_token")
                        .queryParam("client_id", facebook.clientId())
                        .queryParam("client_secret", facebook.clientSecret())
                        .queryParam("redirect_uri", facebook.redirectUri())
                        .queryParam("code", code)
                        .build()
                )
                .retrieve()
                .body(FacebookTokenResponse.class);
    }

    @Override
    public FacebookUserInfoResponse getUserInfo(String accessToken) {
        JsonMapper jsonMapper = new JsonMapper();
        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("graph.facebook.com")
                        .path("/me")
                        .queryParam("fields", "id,name,email,picture")
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .body(String.class);

        FacebookUserInfoResponse userInfo =
                jsonMapper.readValue(response, FacebookUserInfoResponse.class);
        return userInfo;
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        var facebook = appProperties.oauth2().facebook();
        return UriComponentsBuilder
                .fromUriString("https://www.facebook.com/v23.0/dialog/oauth")
                .queryParam("client_id", facebook.clientId())
                .queryParam("redirect_uri", facebook.redirectUri())
                .queryParam("scope", String.join(",", facebook.scope()))
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }
}
