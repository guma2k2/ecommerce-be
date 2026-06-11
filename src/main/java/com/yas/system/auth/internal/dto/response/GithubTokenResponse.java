package com.yas.system.auth.internal.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GithubTokenResponse(
        String accessToken,
        String scope,
        String tokenType
) {
}
