package com.yas.system.auth.internal.dto.response;

import com.yas.system.auth.internal.enums.OauthProvider;

public record OauthUserInfo(
        String id,
        String name,
        String email,
        String imageUrl,
        OauthProvider provider
) {

    public static OauthUserInfo fromGoogleOauthUser(GoogleUserInfoResponse googleUserInfoResponse) {
        return new OauthUserInfo(
                googleUserInfoResponse.sub(),
                googleUserInfoResponse.familyName().concat(" ").concat(googleUserInfoResponse.givenName()),
                googleUserInfoResponse.email(),
                googleUserInfoResponse.picture(),
                OauthProvider.GOOGLE
        );
    }

    public static OauthUserInfo fromGithubOauthUser(GithubUserInfoResponse githubUserInfoResponse, String email) {
        String name = githubUserInfoResponse.name();
        String displayName = name != null && !name.isBlank()
                ? name
                : githubUserInfoResponse.login();
        return new OauthUserInfo(
                githubUserInfoResponse.id().toString(),
                displayName,
                email,
                githubUserInfoResponse.avatarUrl(),
                OauthProvider.GITHUB
        );
    }

    public static OauthUserInfo fromFacebookOauthUser(FacebookUserInfoResponse facebookUserInfoResponse) {
        var imageUrl = facebookUserInfoResponse.picture() != null
                ? facebookUserInfoResponse.picture().data().url()
                : null;
        return new OauthUserInfo(
                facebookUserInfoResponse.id(),
                facebookUserInfoResponse.name(),
                facebookUserInfoResponse.email(),
                imageUrl,
                OauthProvider.FACEBOOK
        );
    }
}
