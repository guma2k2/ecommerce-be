package com.yas.system.auth.internal.dto.response;

public record OauthUserInfo(
        String id,
        String name,
        String email,
        String imageUrl
) {

    public static OauthUserInfo fromGoogleOauthUser(GoogleUserInfoResponse googleUserInfoResponse) {
        return new OauthUserInfo(
                googleUserInfoResponse.sub(),
                googleUserInfoResponse.familyName().concat(" ").concat(googleUserInfoResponse.givenName()),
                googleUserInfoResponse.email(),
                googleUserInfoResponse.picture()
        );
    }

    public static OauthUserInfo fromGithubOauthUser(GithubUserInfoResponse githubUserInfoResponse) {
        return new OauthUserInfo(
                githubUserInfoResponse.id().toString(),
                githubUserInfoResponse.name(),
                githubUserInfoResponse.email(),
                githubUserInfoResponse.avatarUrl()
        );
    }
}
