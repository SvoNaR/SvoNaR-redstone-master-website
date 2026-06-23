package ru.redstonemaster.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RedstoneMasterWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedstoneMasterWebApplication.class, args);
	}
}
