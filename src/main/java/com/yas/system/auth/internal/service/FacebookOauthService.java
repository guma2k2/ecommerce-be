package com.yas.system.auth.internal.service;


import com.yas.system.auth.internal.dto.response.FacebookTokenResponse;
import com.yas.system.auth.internal.dto.response.FacebookUserInfoResponse;

public interface FacebookOauthService {
    FacebookTokenResponse exchangeCodeForToken(String code);
    FacebookUserInfoResponse getUserInfo(String accessToken);
    String buildAuthorizationUrl(String state);
}
