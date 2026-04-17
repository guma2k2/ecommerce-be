package com.yas.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Modulith(sharedModules = {"common"})
@EnableAsync
@EnableScheduling

public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
