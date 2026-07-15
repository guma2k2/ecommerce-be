package com.yas.system.auth.controller;

import com.yas.system.auth.internal.dto.request.UserRequest;
import com.yas.system.auth.internal.dto.response.UserResponse;
import com.yas.system.auth.internal.service.UserService;
import com.yas.system.common.response.ApiResponse;
import com.yas.system.common.response.PageResponse;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/user")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        return ApiResponse.success(userService.createUser(userRequest));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable("id") UUID id) {
        return ApiResponse.success(userService.getUser(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UserRequest userRequest
    ) {
        return ApiResponse.success(userService.updateUser(id, userRequest));
    }

    @GetMapping("/page")
    public ApiResponse<PageResponse<UserResponse>> getUserPage(
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(userService.getUserPage(pageNumber, pageSize));
    }
}
