package com.yas.system.auth.controller;

import com.yas.system.common.security.AuthUser;
import com.yas.system.common.security.annotation.ActiveUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    @GetMapping("/me")
    public void getCurrentUser(
            @ActiveUser AuthUser  authUser
    ){
        log.info("logged user: {}", authUser);
    }
}
