package com.example.aotealApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AotealAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(AotealAppApplication.class, args);
	}

}
