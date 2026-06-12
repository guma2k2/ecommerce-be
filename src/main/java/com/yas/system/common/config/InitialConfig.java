package com.yas.system.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class InitialConfig implements CommandLineRunner {

//    @Value("${GOOGLE_CLIENT_ID:NOT_FOUND}")
//    private String clientId;

    @Override
    public void run(String... args) {
//        System.out.println(clientId);
    }
}