package com.yas.system.auth.internal.service;

import com.yas.system.auth.internal.dto.request.UserRequest;
import com.yas.system.auth.internal.dto.response.UserResponse;
import com.yas.system.common.response.PageResponse;
import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserRequest userRequest);
    UserResponse getUser(UUID id);
    UserResponse updateUser(UUID id, UserRequest userRequest);
    PageResponse<UserResponse> getUserPage(Integer pageNumber, Integer pageSize);
}
