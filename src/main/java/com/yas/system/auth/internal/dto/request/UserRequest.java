package com.yas.system.auth.internal.dto.request;

import java.util.List;

public record UserRequest(
        String email,
        String password,
        String name,
        List<Integer> roleIds
) {
}
