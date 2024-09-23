package com.arcadia.whiteRabbitService;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class WhiteRabbitServiceApplication {
	public static void main(String[] args) {
		SpringApplicationBuilder builder = new SpringApplicationBuilder(WhiteRabbitServiceApplication.class);
		builder.headless(false);
		builder.run(args);
		// https://stackoverflow.com/questions/14038793/headless-exception-in-java
	}
}
