package com.yas.system.auth.internal.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record GoogleUserInfoResponse(
        String sub,
        String email,
        Boolean emailVerified,
        String familyName,
        String givenName,
        String picture
) {}
