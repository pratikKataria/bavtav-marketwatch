package com.goldapp.psoni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PsoniApplication {

	public static void main(String[] args) {
		SpringApplication.run(PsoniApplication.class, args);
	}

}
