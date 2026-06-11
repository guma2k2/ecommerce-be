package com.yas.system.auth.internal.dto.response;

public record GithubEmailResponse(
        String email,
        boolean primary,
        boolean verified
) {
}
