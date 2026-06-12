package com.yas.system.auth.internal.service.impl;

import com.yas.system.auth.internal.dto.response.GithubEmailResponse;
import com.yas.system.auth.internal.dto.response.GithubTokenResponse;
import com.yas.system.auth.internal.dto.response.GithubUserInfoResponse;
import com.yas.system.auth.internal.service.GithubOauthService;
import com.yas.system.common.config.AppProperties;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GithubOauthServiceImpl implements GithubOauthService {

    AppProperties appProperties;
    RestClient restClient;

    @Override
    public GithubTokenResponse exchangeCodeForToken(String code) {
        var github = appProperties.oauth2().github();

        return restClient.post()
                .uri(
                        "https://github.com/login/oauth/access_token"
                )
                .header(
                        HttpHeaders.ACCEPT,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .body(
                        Map.of(
                                "client_id",
                                github.clientId(),
                                "client_secret",
                                github.clientSecret(),
                                "code",
                                code,
                                "redirect_uri",
                                github.redirectUri()
                        )
                )
                .retrieve()
                .body(GithubTokenResponse.class);
    }

    @Override
    public GithubUserInfoResponse getUserInfo(String accessToken) {
        return restClient.get()
                .uri(
                        "https://api.github.com/user"
                )
                .headers(
                        headers ->
                                headers.setBearerAuth(accessToken)
                )
                .retrieve()
                .body(GithubUserInfoResponse.class);
    }

    @Override
    public String buildAuthorizationUrl(String state) {
        var github = appProperties.oauth2().github();

        return UriComponentsBuilder.fromUriString("https://github.com/login/oauth/authorize")
                .queryParam("client_id", github.clientId())
                .queryParam("redirect_uri", github.redirectUri())
                .queryParam("scope", String.join(" ", github.scope()))
                .queryParam("state", state)
                .build()
                .toUriString();
    }

    @Override
    public List<GithubEmailResponse> getEmails(
            String accessToken
    ) {
        return restClient.get()
                .uri(
                        "https://api.github.com/user/emails"
                )
                .headers(
                        headers ->
                                headers.setBearerAuth(accessToken)
                )
                .retrieve()
                .body(
                        new ParameterizedTypeReference<>() {
                        }
                );
    }
}
