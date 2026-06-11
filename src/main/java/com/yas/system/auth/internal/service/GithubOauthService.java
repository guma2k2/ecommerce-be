package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.response.*;

import java.util.List;

public interface GithubOauthService {
    GithubTokenResponse exchangeCodeForToken(String code);
    GithubUserInfoResponse getUserInfo(String accessToken);
    String buildAuthorizationUrl(String state);
    List<GithubEmailResponse> getEmails(String accessToken);
}
