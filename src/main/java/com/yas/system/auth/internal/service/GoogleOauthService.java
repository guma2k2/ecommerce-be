package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.response.GoogleTokenResponse;
import com.yas.system.auth.internal.dto.response.GoogleUserInfoResponse;

public interface GoogleOauthService {
    GoogleTokenResponse exchangeCodeForToken(String code);
    GoogleUserInfoResponse getUserInfo(String accessToken);
    String buildAuthorizationUrl(String state);
}
