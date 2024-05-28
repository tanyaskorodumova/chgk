package com.itmo.chgk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChgkApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChgkApplication.class, args);
	}
}
