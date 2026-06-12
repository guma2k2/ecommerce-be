package com.yas.system.auth.internal.dto.response;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FacebookUserInfoResponse(
        String id,
        String name,
        String email,
        Picture picture
) {
    public record Picture(Data data) {}

    public record Data(
            String url
    ) {}
}